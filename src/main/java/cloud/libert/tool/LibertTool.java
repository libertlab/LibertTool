package cloud.libert.tool;

import cloud.libert.tool.java.JClassEntity;
import cloud.libert.tool.java.JInterface;
import cloud.libert.tool.temp.*;

import java.util.ArrayList;
import java.util.List;

public class LibertTool {
    public static final String VERSION = "1.0";

    LibertToolContext context;
    String lineDividerBegin = ">------------------------------------------------>";
    String lineDivider = ">>";
    String lineDividerEnd = "<------------------------------------------------<";
    long startTime;
    ArrayList<JClassEntity> entityList;
    TemplateDBImpl tempDBCreatorImpl;
    ArrayList<TemplateAccess> tempAccessList;
    ArrayList<TemplateController> tempControllerList;
    ArrayList<TemplateInterfaceDocument> tempInterfaceDocumentList;
    ArrayList<TemplateService> tempServiceList;
    boolean upgradeDB = false;
    boolean createDoc = true;
    ILibertToolCallback callback = ILibertToolCallback.DefalutImpl;

    public LibertTool(LibertToolContext ctx) throws OperatorException {
        this(ctx, false, true);
    }

    public LibertTool(LibertToolContext ctx, boolean upgradeDatabase, boolean createDocument) throws OperatorException {
        System.out.println(lineDividerBegin);
        System.out.println("> LiberTool@" + VERSION + " preparing ...");
        upgradeDB = upgradeDatabase;
        createDoc = createDocument;
        startTime = System.currentTimeMillis();
        context = ctx;
        initDBFiles();
        entityList = new ArrayList<>();
        tempDBCreatorImpl = new TemplateDBImpl(ctx, upgradeDB);
        tempAccessList = new ArrayList<>();
        tempControllerList = new ArrayList<>();
        if (createDocument) {
            tempInterfaceDocumentList = new ArrayList<>();
        }
        tempServiceList = new ArrayList<>();
        upgradeDB = upgradeDatabase;
    }

    public void setCallback(ILibertToolCallback callback) {
        this.callback = callback;
    }

    /**
     * 初始化DBCreatorImpl/DBInitializer/DBUpgrader.java
     */
    private void initDBFiles() {
        TemplateDBInitializer.createFileIfNotExist(context);
        TemplateDBImpl.createFileIfNotExist(context);
        TemplateDBUpgrader.createFileIfNotExist(context);
    }

    public static String getVersion() {
        return VERSION;
    }

    public void addEntity(String... entityNames) throws OperatorException {
        for (int i = 0; i < entityNames.length; i++) {
            addEntity(entityNames[i]);
        }
    }

    public void addEntity(List<String> entityNames) throws OperatorException {
        for (String entityName : entityNames) {
            addEntity(entityName);
        }
    }

    public void addEntity(String entityName) throws OperatorException {
        JClassEntity entity = new JClassEntity(context, context.getEntityPath() + entityName + ".java");
        if (entity.name == null) {
            entity.name = entityName;
        }
        entityList.add(entity);
        tempDBCreatorImpl.update(entity);
        tempAccessList.add(new TemplateAccess(context, entity));
        callback.onAddEntity(entity);
    }

    public void addInterface(String interfaceName) throws OperatorException {
        JInterface jInterface = new JInterface(context.getInterfacesPath() + interfaceName + ".java");
        tempServiceList.add(new TemplateService(context, jInterface));
        tempControllerList.add(new TemplateController(context, jInterface));
        if (createDoc) {
            tempInterfaceDocumentList.add(new TemplateInterfaceDocument(context, jInterface));
        }
        callback.onAddInterface(jInterface);
    }


    public void save() throws OperatorException {
        tempDBCreatorImpl.save();
        System.out.println(lineDivider);
        for (JClassEntity jEntity : entityList) {
            jEntity.save();
        }
        System.out.println(lineDivider);
        for (TemplateAccess access : tempAccessList) {
            access.save();
        }
        System.out.println(lineDivider);
        for (TemplateService service : tempServiceList) {
            service.save();
        }
        System.out.println(lineDivider);
        for (TemplateController controller : tempControllerList) {
            controller.save();
        }
        if (createDoc) {
            System.out.println(lineDivider);
            for (TemplateInterfaceDocument tempDoc : tempInterfaceDocumentList) {
                tempDoc.save();
                callback.onApiDocumentCreated(tempDoc.getSelfPath());
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("> LiberTool@" + VERSION + " finished, timeTaken: " + (endTime - startTime) + "ms.");
        System.out.println(lineDividerEnd);
        callback.onStart();
    }

    public static void main(String[] args) throws OperatorException {
//        LibertToolContext ctx = new LibertToolContext("D:/dev/java/work/jxm_web2020/src/main/", "com.jxm.web");
        LibertToolContext ctx = new LibertToolContext("D:/test/LibertTool/", "com");
        LibertTool tool = new LibertTool(ctx, false, true);
        tool.addEntity("KeyValue");
//        tool.addEntity("JxmCase");
//        tool.addEntity("CaseIndustry");
//        tool.addEntity("CaseScene");
//        tool.addEntity("CaseWords");
//        tool.addInterface("ICaseService");
//        tool.addInterface("IDataService");
        tool.save();

    }
}
