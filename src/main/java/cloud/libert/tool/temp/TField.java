package cloud.libert.tool.temp;

import cloud.libert.tool.core.Formats;
import cloud.libert.tool.util.RegExpr;
import cloud.libert.tool.util.Strings;
import cloud.libert.tool.java.JField;
import cloud.libert.tool.java.MetaAnno;

import java.util.regex.Matcher;

public class TField {
    public String name;
    public String type;
    public int length;//仅对varchar类型有效
    public String defaulValue;//NULL,'0','-1','abc'等
    public String comment;
    public boolean isIndex;//是否是索引字段
    public String typeDef;

    public static final int TYPE_STRING = 1;
    public static final int TYPE_LONG = 2;
    public static final int TYPE_INT = 4;

    public static final RegExpr RE_STR = new RegExpr("^`(\\w+)` +([\\(\\)\\w]+) +DEFAULT +([-'\\w]+)( +COMMENT +'(.*)')?");
    public static final RegExpr RE_LINE = new RegExpr("^tb.add\\( *\"(\\w+)\", *((true|false), *)?type(\\w*)\\(([^\\)]*)\\), *(\"(.*)\"|null) *\\);$");

    private TField() {

    }

    public void diffWith(TField org, TableUpgrader upgrader) {
        if(org.isIndex && !isIndex) {
            upgrader.dropIndex(name);
        } else if(!org.isIndex && isIndex) {
            upgrader.addIndex(name);
        }
        if(!typeDef.equals(org.typeDef)) {
            upgrader.modifyColumn(name, typeDef);
        }
    }

    //"_case_words_id", typeLong(), null
    //"_case_words_id", true, typeLong(), "comment"
    public String toDefineLine() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("\"");
        sb.append(name);
        sb.append("\", ");
        if (isIndex) {
            sb.append("true, ");
        }
        sb.append(typeDef);
        if (comment != null) {
            sb.append(", \"" + comment + "\"");
        } else {
            sb.append(", null");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        //"`_user_name` INT DEFAULT 0 COMMENT ''"
        StringBuilder sb = new StringBuilder(128);
        sb.append("`");
        sb.append(name);
        sb.append("` ");
        sb.append(type);
        sb.append(" DEFAULT ");
        sb.append(defaulValue);
        if (comment != null) {
            sb.append(" COMMENT '");
            sb.append(comment);
            sb.append("'");
        }
        return sb.toString();
    }

    public TField(JField jf) {
        name = Formats.toTableFieldName(jf.name);
        comment = jf.desc;
        String orgType = jf.type;
        String dv = jf.defaultValue;
        MetaAnno anno = jf.getAnno();
        isIndex = anno.hasTrue("dbmap", "isIndex");
        switch (orgType) {
            case "String":
                String len = anno.getValue("dbmap", "length", "255");
                length = Integer.parseInt(len);
                type = "VARCHAR(" + length + ")";
                String tDefVal = "";
                if (dv != null) {
                    tDefVal = dv;
                    defaulValue = dv.replaceAll("\"", "'");
                } else {
                    defaulValue = "NULL";
                }

                if(length>4096) {
                    type = "TEXT";
                    typeDef = "typeText()";
                } else {
                    String tArg = length == 255 ? "" : Integer.toString(length);
                    if (tDefVal.length() > 0) {
                        if (tArg.length() > 0) {
                            tArg += ", ";
                        }
                        tArg += tDefVal;
                    }
                    typeDef = "typeString(" + tArg + ")";
                }
                break;
            case "long":
                type = "BIGINT";
                if (dv != null) {
                    defaulValue = "'" + dv + "'";
                    typeDef = "typeLong(" + dv + ")";
                } else {
                    defaulValue = "'0'";
                    typeDef = "typeLong()";
                }
                break;
            case "int":
                type = "INT";
                if (dv != null) {
                    defaulValue = "'" + dv + "'";
                    typeDef = "typeInt(" + dv + ")";
                } else {
                    defaulValue = "'0'";
                    typeDef = "typeInt()";
                }
                break;
            default://不支持的类型暂时都归为VARCHAR
                type = "VARCHAR(255)";
                length = 255;
                defaulValue = "NULL";
                typeDef = "typeString()";
                break;
        }
    }

    public static TField fromString(String line) {
        TField tf = null;
        Matcher mch = RE_STR.findMatcher(line);
        if (mch != null) {
            tf = new TField();
            tf.name = mch.group(1);
            String type = mch.group(2).toUpperCase();
            tf.type = type;
            tf.defaulValue = mch.group(3);
            if (mch.group(5) != null) {
                tf.comment = mch.group(5);
            }

            String lenStr = null;
            if (type.startsWith("VARCHAR")) {
                lenStr = Strings.findFirst(type, 0, '(', ')');
            }
            if (lenStr != null) {
                tf.length = Integer.parseInt(lenStr);
            } else {
                tf.length = 255;
            }
        }
        return tf;
    }

    public static TField fromDefineLine(String line) {
        TField tf = null;
        Matcher mch = RE_LINE.findMatcher(line);
        if (mch != null) {
            tf = new TField();
            tf.name = mch.group(1);

            if("true".equals(mch.group(3))) {
                tf.isIndex = true;
            }
            parseType(tf, mch.group(4), mch.group(5));
            tf.comment = mch.group(7);//maybe null
        }
        return tf;
    }

    private static void parseType(TField tf, String typeStr, String argStr) {
        argStr = argStr.trim();
        String[] args = null;
        if(argStr.length()>0) {
            args = argStr.split(",");
            for(int i=0;i<args.length;i++) {
                args[i] = args[i].trim();
            }
        }
        switch (typeStr) {
            case "Int":
                tf.type = "INT";
                if(args!=null) {
                    tf.defaulValue = args[0];
                } else {
                    tf.defaulValue = "0";
                }
                tf.typeDef = "typeInt(" + argStr + ")";
                break;
            case "Long":
                tf.type = "BIGINT";
                if(args!=null) {
                    tf.defaulValue = args[0];
                } else {
                    tf.defaulValue = "0";
                }
                tf.typeDef = "typeLong(" + argStr + ")";
                break;
            case "String":
                if(args!=null) {
                    if(args.length>=2) {
                        tf.type = "VARCHAR(" + args[0] + ")";
                        tf.length = Integer.parseInt(args[0]);
                        tf.defaulValue = args[1];
                    } else if(args.length>=1) {
                        tf.type = "VARCHAR(" + args[0] + ")";
                        tf.length = Integer.parseInt(args[0]);
                        tf.defaulValue = "NULL";
                    }
                    tf.typeDef = "typeString(" + argStr + ")";
                } else {
                    tf.type = "VARCHAR(255)";
                    tf.length = 255;
                    tf.typeDef = "typeString()";
                }
                break;
            case "Text":
                tf.type = "TEXT";
                tf.defaulValue = "NULL";
                tf.typeDef = "typeText()";
                break;
            default:
                break;
        }
    }
}
