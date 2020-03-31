package cloud.libert.tool.temp;

import cloud.libert.tool.core.Formats;
import cloud.libert.tool.java.JClassEntity;
import cloud.libert.tool.java.JField;
import cloud.libert.tool.java.JMethod;
import cloud.libert.tool.java.MetaAnno;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TTable {
    public String name;
    public String entityName;
    //不含ID字段
    public ArrayList<TField> fields = new ArrayList<>();
    public Map<String, TField> fieldsMap = new HashMap<>();
    public static final String createMethodPrefix = "createTable";
    public static final String fieldLinePrefix = "        tb.add(";

    private TTable() {

    }

    public TField getField(String name) {
        return fieldsMap.get(name);
    }

    public TTable(JClassEntity entity) {
        entityName = entity.name;
        name = Formats.toTableName(entityName);
        //去掉排第一位的ID字段
        JField jf;
        TField tf;
        for (int i = 1; i < entity.fields.size(); i++) {
            jf = entity.fields.get(i);

            MetaAnno anno = jf.getAnno();
            if (anno.hasFalse("dbmap", "isMap")) {
                continue;//跳过不用映射到数据库的字段
            }
            tf = new TField(jf);
            fieldsMap.put(tf.name, tf);
            fields.add(tf);
        }
    }

    public static TTable fromJMethod(JMethod jm) {
        int prefixLen = createMethodPrefix.length();
        TTable table = null;
        if (jm.name.length() > prefixLen) {
            table = new TTable();
            table.entityName = jm.name.substring(prefixLen);
            table.name = Formats.toTableName(table.entityName);
            String line;
            char c;
            final int len = fieldLinePrefix.length();
            String fn;
            TField tf;
            ArrayList<String> jmBody = jm.getBody();
            for (int i = 0; i < jmBody.size(); i++) {
                line = jmBody.get(i).trim();
                if (line.startsWith("tb.add")) {
                    tf = TField.fromDefineLine(line);
                    table.fields.add(tf);
                    table.fieldsMap.put(tf.name, tf);
                }
            }
        }
        return table;
    }

    //TableBuilder tb = new TableBuilder("case_words_case");
    //        tb.add("_case_words_id", typeLong(), null);
    //        stat.executeUpdate(tb.build());
    public JMethod toJMethod() {
        String line = "protected void " + createMethodPrefix + entityName + "(Statement stat) throws SQLException {";
        JMethod jm = new JMethod(line);
        ArrayList<String> body = jm.getBody();
        body.add("        TableBuilder tb = new TableBuilder(\"" + name + "\");");
        for (TField tf : fields) {
            body.add(fieldLinePrefix + tf.toDefineLine() + ");");
        }
        body.add("        stat.executeUpdate(tb.build());");
        return jm;
    }


    TableUpgrader diffWith(TTable t) {
        if (!name.equals(t.name)) return null;

        TableUpgrader upgrader = new TableUpgrader(name);
        TField org;
        String afterOf = Formats.TABLE_ID;
        for (TField tf : fields) {
            org = t.getField(tf.name);
            if(org!=null) {//检查是否需要修改
                tf.diffWith(org, upgrader);
            } else {//新增
                upgrader.addColumn(tf.name, tf.typeDef, afterOf);
            }
            afterOf = tf.name;
        }
        return upgrader.opSize()>0? upgrader : null;
    }

}
