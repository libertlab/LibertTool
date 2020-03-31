package cloud.libert.tool.temp;

import cloud.libert.tool.LibertToolContext;
import cloud.libert.tool.OperatorException;
import cloud.libert.tool.java.JClass;
import cloud.libert.tool.java.JMethod;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TemplateDBUpgrader {
    public static final String NAME = "DBUpgrader";
    public static int currentVer;
    private static final List<String> tableAdded = new ArrayList<>();
    private static final List<TableUpgrader> tableUpgraders = new ArrayList<>();

    public static void addCreateTableLine(String line) {
        tableAdded.add(line);
    }
    public static void addTableUpgrader(TableUpgrader upgrader) {
        tableUpgraders.add(upgrader);
    }

    public static void createFileIfNotExist(LibertToolContext context) {
        File file = new File(context.getDBPath() + NAME + ".java");
        BufferedWriter bw = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
                bw = new BufferedWriter(new FileWriter(file));
                writeInitContent(context, bw);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void save(LibertToolContext ctx) throws OperatorException {
        String path = ctx.getDBPath() + NAME + ".java";
        JClass parser = new JClass(path);
        if (parser.loadedFromFile) {
            if (tableAdded.size() > 0 || tableUpgraders.size() > 0) {
                if(upgrade(parser)) {
                    parser.writeToFile(path);
                    System.out.println("> " + ctx.getDBPackage() + "." + NAME + " updated.");
                }
            }
        }
    }

    private static boolean upgrade(JClass parser) throws OperatorException {
        JMethod processMethod = null;
        int proMethodIndex = -1;
        for(int i=0;i<parser.methods.size();i++) {
            JMethod jm = parser.methods.get(i);
            if("upgradeProcess".equals(jm.name)) {
                proMethodIndex = i;
                processMethod = jm;
                break;
            }
        }
        if(processMethod==null) {
            System.out.println("> WARNING: DBUpgrader.upgradeProcess() is not found...");
            return false;
        }

        String methodName;
        do {
            methodName = "upgradeTo" + (++currentVer);
        } while (parser.hasMethod(methodName));

        processMethod.appendBodyLine("        "+methodName+"(stat, dbVersion, newVersion);");
        JMethod jm = new JMethod("private void " + methodName + "(Statement stat, int dbVersion, int newVersion) throws SQLException {");
        jm.appendBodyLine("        if (dbVersion <= " + (currentVer - 1) + " && newVersion == " + currentVer + ") {");
        for (TableUpgrader upgrader : tableUpgraders) {
            for (String op : upgrader.build()) {
                jm.appendBodyLine("            " + op);
            }
        }
        for (String toAdd : tableAdded) {
            jm.appendBodyLine("    " + toAdd);
        }
        jm.appendBodyLine("        }");
        parser.addMethod(jm, proMethodIndex+1);
        parser.writeToFile(parser.mCodeFilePath);
        return true;
    }

    private static void writeInitContent(LibertToolContext ctx, BufferedWriter bw) throws IOException {
        bw.write("package " + ctx.getDBPackage() + ";\r\n");
        bw.write("\r\n");
        bw.write("import com.libert.database.TableAlteration;\r\n");
        bw.write("import org.apache.commons.logging.Log;\r\n");
        bw.write("import org.apache.commons.logging.LogFactory;\r\n");
        bw.write("\r\n");
        bw.write("import java.sql.SQLException;\r\n");
        bw.write("import java.sql.Statement;\r\n");
        bw.write("\r\n");
        bw.write("public class " + NAME + " extends DBImpl {\r\n");
        bw.write("    private Log logger = LogFactory.getLog(getClass());\r\n");
        bw.write("\r\n");
        bw.write("    public " + NAME + "(IDataBaseConf dbConf) {\r\n");
        bw.write("        super(dbConf);\r\n");
        bw.write("    }\r\n");
        bw.write("\r\n");
        bw.write("    public int upgrade(Statement stat, int dbVersion, int newVersion) throws SQLException {\r\n");
        bw.write("        int rc = dbVersion;\r\n");
        bw.write("        try {\r\n");
        bw.write("            for (int i = dbVersion + 1; i <= newVersion; i++) {\r\n");
        bw.write("                upgradeProcess(stat, i - 1, i);\r\n");
        bw.write("                rc = i;\r\n");
        bw.write("            }\r\n");
        bw.write("        } catch (Exception e) {\r\n");
        bw.write("            logger.error(\"upgrade db:\" + super.getName() + \" failed...version=\" + rc + \", expectedVer=\" + newVersion, e);\r\n");
        bw.write("        }\r\n");
        bw.write("        return rc;\r\n");
        bw.write("    }\r\n");
        bw.write("\r\n");
        bw.write("    private TableAlteration alter(String tableName) {\r\n");
        bw.write("        return new TableAlteration(tableName);\r\n");
        bw.write("    }\r\n");
        bw.write("\r\n");
        bw.write("    private void upgradeProcess(Statement stat, int dbVersion, int newVersion) throws SQLException {\r\n");
        bw.write("    }\r\n");
        bw.write("}\r\n");
    }

}
