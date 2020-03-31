package cloud.libert.tool.temp;

import cloud.libert.tool.OperatorException;
import cloud.libert.tool.core.Formats;
import cloud.libert.tool.LibertToolContext;
import cloud.libert.tool.java.*;

public class TemplateAccess {
	LibertToolContext context;
	String entityName;
	String selfName;
	String tableName;
	String selfPath;
	JClass parser;
	JClassEntity entity;

	public TemplateAccess(LibertToolContext ctx, JClassEntity entity) throws OperatorException {
		context = ctx;
		this.entity = entity;
		entityName = entity.name;
		tableName = Formats.toTableName(entityName);
		selfName = entityName + "Access";
		selfPath = context.getAccessPath() + selfName + ".java";

		parser = new JClass(selfPath);
		if(!parser.loadedFromFile) {
			initTemplate();
		} else {
			updateTemplate();
		}
	}

	public void save() throws OperatorException {
		parser.writeToFile(selfPath);
		String tip = "  created.";
		if(parser.loadedFromFile) {
			tip = "  updated.";
		}
		System.out.println("> " + context.getAccessPackage() + "." + selfName + tip);
	}

	private void initTemplate() {
		parser.fileDescs.add(JDesc.fileDesc());
		parser.mPackage = context.getAccessPackage();
		parser.mImports.add(context.getEntityPackage() + "." + entityName);
		parser.mImports.add("com.libert.database.BaseAccess");
		parser.name = selfName;
		parser.defLine = "public class "+selfName+" extends BaseAccess<"+entityName+"> {";

		initFieldsAndContructor();
		parser.addOrUpdateMethod(createOtherInitMethod());
	}

	private void updateTemplate() {
		parser.removeFieldsStartsWith("col_");
		initFieldsAndContructor();
	}

	private void initFieldsAndContructor() {
		JMethod contructor = new JMethod("private "+selfName+"() {");
		contructor.appendBodyLine("        entityName = \""+entityName+"\";");
		contructor.appendBodyLine("        tableName = \""+tableName+"\";");
		contructor.appendBodyLine("        entityClazz = "+entityName+".class;");

		parser.addOrUpdateField(new JField("private static volatile "+selfName+" selfInstance;"));
		String columnName;
		String keyName;
		MetaAnno anno;
		for(JField jf : entity.fields) {
			anno = jf.getAnno();
			if(anno.hasFalse("dbmap", "isMap")) {
				continue;
			}
			columnName = Formats.toTableFieldName(jf.name);
			if(columnName.startsWith("_")) {
				keyName = "col" + columnName;
			} else {
				keyName = "col_" + columnName;
			}
			parser.addField(new JField("public static final String "+keyName+" = \"" +columnName+ "\";"));
			if(!Formats.TABLE_ID.equals(columnName)) {
				contructor.appendBodyLine("        fieldsMap.put("+keyName+", \""+jf.name+"\");");
			}
		}
		contructor.appendBodyLine("        otherInit();");

		parser.addOrUpdateMethod(contructor);
		parser.addOrUpdateMethod(createSelfMethod());
	}

	private JMethod createOtherInitMethod() {
		JMethod jm = new JMethod("private void otherInit() {");
		return jm;
	}

	private JMethod createSelfMethod() {
		JMethod jm = new JMethod("public static "+selfName+" self() {");
		jm.appendBodyLine("		if (selfInstance == null) {");
		jm.appendBodyLine("		    synchronized ("+selfName+".class) {");
		jm.appendBodyLine("                if (selfInstance == null) {");
		jm.appendBodyLine("                    selfInstance = new "+selfName+"();");
		jm.appendBodyLine("                }");
		jm.appendBodyLine("            }");
		jm.appendBodyLine("		}");
		jm.appendBodyLine("		return selfInstance;");
		return jm;
	}



}
