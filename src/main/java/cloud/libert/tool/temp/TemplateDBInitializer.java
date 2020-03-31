package cloud.libert.tool.temp;

import cloud.libert.tool.LibertToolContext;
import cloud.libert.tool.core.Formats;

import java.io.*;

public class TemplateDBInitializer {
    public static final String name = "DBInitializer";

    public static void createFileIfNotExist(LibertToolContext context) {
        File file = new File(context.getDBPath() +name+".java");
        if(!file.exists()) {
            try(BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(generateContent(context));
                System.out.println("> " + context.getDBPackage() + "." + name + "  created.");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("> " + context.getDBPackage() + "." + name + "  already exists.");
        }
    }

    private static String generateContent(LibertToolContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ");
        sb.append(context.getDBPackage());
        sb.append(";");
        sb.append(Formats.NL2);
        sb.append("import java.sql.Statement;");
        sb.append(Formats.NL2);
        sb.append("public class DBInitializer {");
        sb.append(Formats.NL2);
        sb.append("    public void initData(Statement stat) {");
        sb.append(Formats.NL);
        sb.append("    }");
        sb.append(Formats.NL);
        sb.append("}");
        sb.append(Formats.NL);
        return sb.toString();
    }
}
