package cloud.libert.tool.temp;

import cloud.libert.tool.LibertToolContext;
import cloud.libert.tool.LibertToolException;
import cloud.libert.tool.core.Formats;
import cloud.libert.tool.util.Strings;
import cloud.libert.tool.java.*;

import java.util.ArrayList;
import java.util.List;

public class TemplateController {
    LibertToolContext context;
    String keyName;
    String interfaceName;
    String serviceName;
    JInterface jInterface;


    String selfName;
    String selfPath;
    JClass parser;

    public TemplateController(LibertToolContext ctx, JInterface jInterface) throws LibertToolException {
        interfaceName = jInterface.name;
        this.context = ctx;
        this.jInterface = jInterface;

        keyName = Strings.strip(interfaceName, "I", "Service");
        selfName = keyName + "Controller";
        serviceName = keyName + "Service";
        selfPath = context.getControllerApiPath() + selfName + ".java";

        parser = new JClass(selfPath);
        if(!parser.loadedFromFile) {
            initTemplate();
        } else {
            updateTemplate();
        }
    }


    public void save() throws LibertToolException {
        parser.writeToFile(selfPath);
        String tip = "  created.";
        if(parser.loadedFromFile) {
            tip = "  updated.";
        }
        System.out.println("> " + context.getControllerApiPackage() + "." + selfName + tip);
    }

    private void initTemplate() {
        parser.fileDescs.add(JDesc.fileDesc());
        parser.mPackage = context.getControllerApiPackage();
        parser.mImports.add(context.getServicePackage() + "." + serviceName);
        parser.mImports.add(context.getInterfacesPackage() + "." + interfaceName);
        parser.mImports.add("com.libert.core.Response");
        parser.name = selfName;
        parser.defLine = "public class "+selfName+ " {";
        parser.classAnnos.add("@RestController");
        parser.classAnnos.add("@RequestMapping(\"/api/"+keyName.toLowerCase()+"\")");

        initFieldsAndContructor();
        parser.addOrUpdateMethod(createInitMethod());
        updateInterfacesMethod();
    }

    private void updateTemplate() {
        parser.removeFieldsByType(serviceName);
        initFieldsAndContructor();
        updateInterfacesMethod();
    }

    private void initFieldsAndContructor() {
        parser.addField(new JField("private final "+serviceName+" service;"));

        JMethod contructor = new JMethod("public "+selfName+"("+serviceName+" service) {");
        contructor.addAnnoLine("@Autowired");
        contructor.appendBodyLine("        this.service = service;");
        contructor.appendBodyLine("        init();");
        parser.addOrUpdateMethod(contructor);
    }

    private JMethod createInitMethod() {
        JMethod jm = new JMethod("private void init() {");
        return jm;
    }

    private void updateInterfacesMethod() {
        ArrayList<JInterfaceMethod> list = jInterface.getMethods();
        for(JInterfaceMethod method : list) {
            parser.addOrUpdateMethod(makeControllerMethod(method));
        }
    }

    private JMethod makeControllerMethod(JInterfaceMethod method) {
        JMethod jm = new JMethod(method.name, "Response");
        ArrayList<String> annoLines = new ArrayList<>();
        annoLines.add("@PostMapping(\"/"+method.name+"\")");
        jm.annoLines = annoLines;
        jm.restPart = method.restPart.replaceFirst(";", " {");


        jm.appendBodyLine("        try {");
        JType type = new JType(method.returnType);
        StringBuilder rtLine = new StringBuilder(128);
        rtLine.append("            ");
        if(type.isResponse) {
            rtLine.append("return ");
        } else if(!type.isVoid) {
            rtLine.append("return Response.ok()");
            if(type.isList) {
                rtLine.append(".setDatas(");
            } else {
                rtLine.append(".putData(");
            }
        }
        rtLine.append("service.");
        rtLine.append(jm.name);
        rtLine.append("(");

        boolean in = false;
        MetaAnno anno;
        MetaArg arg;
        List<MetaArg> argList = method.getArgList();
        if(argList != null) {
            try {
                for(MetaArg metaArg : argList) {
                    arg = metaArg.copy();
                    anno = arg.anno;
                    if(anno==null) {
                        anno = new MetaAnno();
                        arg.anno = anno;
                    }
                    anno.setValue("RequestParam", "value", "\""+ Formats.forShort(arg.name)+"\"");
                    jm.addArg(arg);
                    if(in) {
                        rtLine.append(", ");
                    }
                    rtLine.append(arg.name);
                    in = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        rtLine.append(")");
        if(type.isVoid) {
            rtLine.append(";");
            jm.appendBodyLine(rtLine.toString());
            jm.appendBodyLine("            return Response.ok();");
        } else {
            if(!type.isResponse) {
                rtLine.append(")");
            }
            rtLine.append(";");
            jm.appendBodyLine(rtLine.toString());
        }
        jm.appendBodyLine("        } catch (OperatorException e) {");
        jm.appendBodyLine("            e.printStackTrace();");
        jm.appendBodyLine("            return Response.of(e.statusCode());");
        jm.appendBodyLine("        }");
        return jm;
    }

}
