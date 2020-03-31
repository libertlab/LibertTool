package cloud.libert.tool.temp;

import cloud.libert.tool.LibertToolContext;
import cloud.libert.tool.LibertToolException;
import cloud.libert.tool.util.Strings;
import cloud.libert.tool.java.*;

import java.util.ArrayList;
import java.util.List;

public class TemplateService {
    LibertToolContext context;
    String keyName;
    String interfaceName;
    JInterface jInterface;

    String selfName;
    String selfPath;
    JClass parser;

    public TemplateService(LibertToolContext ctx, JInterface jInterface) throws LibertToolException {
        interfaceName = jInterface.name;
        this.context = ctx;
        this.jInterface = jInterface;

        keyName = Strings.strip(interfaceName, "I", "Service");
        selfName = keyName + "Service";
        selfPath = context.getServicePath() + selfName + ".java";

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
        System.out.println("> " + context.getServicePackage() + "." + selfName + tip);
    }

    private void initTemplate() {
        parser.fileDescs.add(JDesc.fileDesc());
        parser.mPackage = context.getServicePackage();
        parser.mImports.add(context.getInterfacesPackage() + "." + interfaceName);
        parser.mImports.add("org.springframework.stereotype.Service");

        parser.name = selfName;
        parser.defLine = "public class "+selfName+" implements "+interfaceName+" {";
        parser.classAnnos.add("@Service");

        updateInterfacesMethod();
    }

    private void updateTemplate() {
        updateInterfacesMethod();
    }

    private void updateInterfacesMethod() {
        ArrayList<JInterfaceMethod> list = jInterface.getMethods();
        for(JInterfaceMethod method : list) {
//            if(!parser.hasMethod(method.name)) {
//                parser.addMethod(makeControllerMethod(method));
//            }
            parser.addOrUpdateMethodSignature(makeControllerMethod(method));
        }
    }

    private JMethod makeControllerMethod(JInterfaceMethod method) {
        JMethod jm = new JMethod(method.name, method.returnType);
        ArrayList<String> annoLines = new ArrayList<>();
        annoLines.add("@Override");
        jm.annoLines = annoLines;
        jm.restPart = method.restPart.replaceFirst(";", " {");


        List<MetaArg> argList = method.getArgList();
        if(argList != null) {
            try {
                for(MetaArg arg : argList) {
                    jm.addArg(arg.copy());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        JType type = new JType(jm.returnType);
        jm.appendBodyLine("        //TODO...");
        if(!type.isVoid) {
            String rt = type.isNumber ? "0" : "null";
            jm.appendBodyLine("        return "+rt+";");
        }
        return jm;
    }

}
