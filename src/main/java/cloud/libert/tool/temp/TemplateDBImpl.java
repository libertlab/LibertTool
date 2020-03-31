package cloud.libert.tool.temp;

import cloud.libert.tool.LibertToolContext;
import cloud.libert.tool.OperatorException;
import cloud.libert.tool.java.JClass;
import cloud.libert.tool.java.JClassEntity;
import cloud.libert.tool.java.JField;
import cloud.libert.tool.java.JMethod;

import java.io.*;

public class TemplateDBImpl {
    LibertToolContext context;
    static String selfName = "DBImpl";
    String selfPath;
    JClass selfParser;
    JMethod jmCreateTable = null;
    JField jfDBVersion = null;
    boolean upgradeDb = false;
    int dbVersion = 1;

    public TemplateDBImpl(LibertToolContext ctx, boolean upgradeDb) throws OperatorException {
        context = ctx;
        this.upgradeDb = upgradeDb;
        selfPath = ctx.getDBPath() + selfName + ".java";
        selfParser = new JClass(selfPath);
        if (selfParser.loadedFromFile) {
            for (JMethod jm : selfParser.methods) {
                if ("createTable".equals(jm.name)) {
                    jmCreateTable = jm;
                    break;
                }
            }
            for (JField jf : selfParser.fields) {
                if ("dbVersion".equals(jf.name)) {
                    jfDBVersion = jf;
                    dbVersion = Integer.parseInt(jf.defaultValue);
                    TemplateDBUpgrader.currentVer = Integer.parseInt(jf.defaultValue);
                    break;
                }
            }
        } else {
            throw new OperatorException("File not found: " + selfPath);
        }
        if (jmCreateTable == null) {
            throw new OperatorException("Cannot find member method: " + selfName + ".createTable(Statement stat)");
        }
        if (jfDBVersion == null) {
            throw new OperatorException("Cannot find member field: " + selfName + ".dbVersion");
        }
    }

    public void update(JClassEntity entity) {
        TTable table = new TTable(entity);
        JMethod jm = table.toJMethod();
        String callLine = "        createTable" + entity.name + "(stat);";
        JMethod orgMethod = selfParser.getMethod(jm.name);
        if (orgMethod != null) {
            selfParser.updateMethod(jm);
            if (upgradeDb) {
                TTable orgTable = TTable.fromJMethod(orgMethod);
                TableUpgrader tableUpgrader = table.diffWith(orgTable);
                if (tableUpgrader != null) {
                    TemplateDBUpgrader.addTableUpgrader(tableUpgrader);
                }
            }
        } else {
            selfParser.addMethod(jm);
            if (jmCreateTable != null) {
                jmCreateTable.appendBodyLine(callLine);
            }
            if (upgradeDb) {
                TemplateDBUpgrader.addCreateTableLine(callLine);
            }
        }
    }

    public void save() throws OperatorException {
        if (upgradeDb) {
            TemplateDBUpgrader.save(context);
            //更新版本号
            int newVersion = TemplateDBUpgrader.currentVer;
            if (newVersion > dbVersion) {
                jfDBVersion.defaultValue = "" + newVersion;
                jfDBVersion.defLine = "protected final int dbVersion = " + newVersion + ";";
                System.out.println("> " + context.getDBPackage() + "." + selfName + " updated.");
                System.out.println("> " + selfName + ".dbVersion upgraded: " + dbVersion + "->" + newVersion);
            }
        }
        selfParser.writeToFile(selfPath);
        System.out.println("> " + context.getDBPackage() + "." + selfName + " updated.");
    }

    public static void createFileIfNotExist(LibertToolContext context) {
        File file = new File(context.getDBPath() + selfName + ".java");
        if (!file.exists()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                writeInitContent(context, bw);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void writeInitContent(LibertToolContext ctx, BufferedWriter bw) throws IOException {
        bw.write("package " + ctx.getDBPackage() + ";\r\n");
        bw.write("\r\n");
        bw.write("import com.libert.database.IDataBase;\r\n");
        bw.write("import com.libert.database.IDataBaseConf;\r\n");
        bw.write("import org.apache.commons.logging.Log;\r\n");
        bw.write("import org.apache.commons.logging.LogFactory;\r\n");
        bw.write("\r\n");
        bw.write("import java.sql.SQLException;\r\n");
        bw.write("import java.sql.Statement;\r\n");
        bw.write("\r\n");
        bw.write("public class DBImpl implements IDataBase {\r\n");
        bw.write("    private Log logger = LogFactory.getLog(getClass());\r\n");
        bw.write("    protected final IDataBaseConf dbConf;\r\n");
        bw.write("    protected final int dbVersion = 1;\r\n");
        bw.write("    protected final String dbName;\r\n");
        bw.write("\r\n");
        bw.write("    public DBImpl(IDataBaseConf dbConf) {\r\n");
        bw.write("        this.dbConf = dbConf;\r\n");
        bw.write("        dbName = dbConf.getDbName();\r\n");
        bw.write("    }\r\n");
        bw.write("\r\n");
        bw.write("    @Override\r\n");
        bw.write("    public int upgrade(Statement stat, int dbVersion, int newVersion) throws SQLException {\r\n");
        bw.write("        DBUpgrader dbUpgrader = new DBUpgrader(dbConf);\r\n");
        bw.write("        return dbUpgrader.upgrade(stat, dbVersion, newVersion);\r\n");
        bw.write("    }\r\n");
        bw.write("\r\n");
        bw.write("    @Override\r\n");
        bw.write("    public void initData(Statement stat) {\r\n");
        bw.write("        try {\r\n");
        bw.write("            DBInitializer initor = new DBInitializer();\r\n");
        bw.write("            initor.initData(stat);\r\n");
        bw.write("        } catch (Exception e) {\r\n");
        bw.write("            logger.error(\"initData failed...\", e);\r\n");
        bw.write("        }\r\n");
        bw.write("    }\r\n");
        bw.write("\r\n");
        bw.write("    @Override\r\n");
        bw.write("    public String getName() {\r\n");
        bw.write("        return dbName;\r\n");
        bw.write("    }\r\n");
        bw.write("\r\n");
        bw.write("    @Override\r\n");
        bw.write("    public int getCurrentVersion() {\r\n");
        bw.write("        return dbVersion;\r\n");
        bw.write("    }\r\n");
        bw.write("\r\n");
        bw.write("    @Override\r\n");
        bw.write("    public void createTable(Statement stat) throws SQLException {\r\n");
        bw.write("    }\r\n");
        bw.write("}\r\n");
    }

}
