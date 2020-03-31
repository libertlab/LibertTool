package cloud.libert.tool.temp;

import cloud.libert.tool.LibertToolContext;
import cloud.libert.tool.OperatorException;
import cloud.libert.tool.core.Formats;
import cloud.libert.tool.util.Strings;
import cloud.libert.tool.java.JInterface;
import cloud.libert.tool.java.JInterfaceMethod;
import cloud.libert.tool.java.MetaArg;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 * 生成接口文档.md
 * 抽取方法上面的注释
 * url和参数
 * 默认放到resources/static/doc目录下，支持放到本地其他目录，
 * 或者自定义生成成功后的回调，通过ftp、文件上传等方式弄到其他共享的远程位置
 */
public class TemplateInterfaceDocument {
    LibertToolContext context;
    String keyName;
    String interfaceName;
    JInterface jInterface;

    String selfName;
    String selfPath;
    String localUrl = "";
    String testEnvUrl = "";
    String onlineEnvUrl = "";
    String version = "V1.0";

    public TemplateInterfaceDocument(LibertToolContext ctx, JInterface jInterface) throws OperatorException {
        interfaceName = jInterface.name;
        this.context = ctx;
        this.jInterface = jInterface;

        keyName = Strings.strip(interfaceName, "I", "Service");
        selfName = keyName + "Service";
        selfPath = context.getApiDocumentPath() + selfName + "-api.md";
    }

    public String getSelfPath() {
        return selfPath;
    }

    public void save() throws OperatorException {
        boolean isUpdate = false;

        BufferedWriter bw = null;
        try {
            File file = new File(selfPath);
            if (!file.exists()) {
                file.createNewFile();
            } else {
                isUpdate = true;
                parseDocMeta(file);
            }
            bw = new BufferedWriter(new FileWriter(file));
            bw.write("## " + selfName +"接口文档" + version);
            bw.write(Formats.NL2);
            bw.write("> **基础URL**"+Formats.NL);
            bw.write("开发本机："+localUrl+Formats.NL);
            bw.write("测试环境："+testEnvUrl+Formats.NL);
            bw.write("正式环境："+onlineEnvUrl);
            bw.write(Formats.NL2);
            writeInterfaceMethodDocument(bw);
        } catch (IOException e) {
            e.printStackTrace();
            throw new OperatorException(e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String tip = isUpdate ? "updated." : "created.";
        System.out.println("> " + context.getApiDocumentPath() + selfName + ".md api-document " + tip);
    }

    private void parseDocMeta(File file) {
        try(BufferedReader bw = new BufferedReader(new FileReader(file))) {


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeInterfaceMethodDocument(BufferedWriter bw) throws IOException {
        ArrayList<JInterfaceMethod> list = jInterface.getMethods();
        for (int i = 0; i < list.size(); i++) {
            writeMethodDocument(i, list.get(i), bw);
        }
    }

    private void writeMethodDocument(int index, JInterfaceMethod method, BufferedWriter bw) throws IOException {
        bw.write("### "+(++index)+". " + "`/api/" + keyName.toLowerCase()+"/" + method.name+"`");
        bw.write(Formats.NL);

        if(method.descTop != null) {
            method.descTop.wirteTo(bw);
        }
        bw.write(Formats.NL);
        bw.write("参数："+Formats.NL);

        List<MetaArg> argList = method.getArgList();
        if (argList != null) {
            try {
                for (MetaArg arg : argList) {
                    bw.write("        `"+Formats.forShort(arg.name)+"`--"+arg.name+"，"+arg.type.toLowerCase()+"类型");
                    bw.write(Formats.NL);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        bw.write("返回值："+method.returnType);
        bw.write(Formats.NL2);
    }

}
