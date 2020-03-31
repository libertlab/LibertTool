package cloud.libert.tool;

import cloud.libert.tool.util.Strings;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LibertToolContext {
    private String basePath;//absolute path of 'src/main/'
    private String basePackage;
    private String javaCodePath;//src/main/java/[package]/
    private String resourcesPath;//src/main/resources/

    public LibertToolContext(String basePath, String basePackge) {
        String p = Strings.replaceAll(basePath, '\\', '/');
        if (!p.endsWith("/")) {
            p += "/";
        }
        this.basePath = p;
        this.basePackage = basePackge;
        javaCodePath = p + "java/" + Strings.replaceAll(basePackge, '.', '/') + "/";
        resourcesPath = p + "resources/";
    }

    //一些配置项
    public static String dbPackage = "dbPackage";
    public static String entityPackage = "entityPackage";
    public static String accessPackage = "accessPackage";
    public static String servicePackage = "servicePackage";
    public static String interfacesPackage = "interfacesPackage";
    public static String apiControllerPackage = "apiControllerPackage";
    public static String apiDocumentPath = "apiDocumentPath";
    private static Map<String, String> configMap = new HashMap<>();

    static {
        configMap.put(dbPackage, "db");
        configMap.put(entityPackage, "db.entity");
        configMap.put(accessPackage, "db.access");
        configMap.put(servicePackage, "service");
        configMap.put(interfacesPackage, "service.interfaces");
        configMap.put(apiControllerPackage, "controller.api");
        /**
         * 默认：src/main/resources/static/api-doc
         * 接口描述文档位置
         *  -可以指定基于resources的相对位置，如"static/my-api-doc"
         *  -可以指定绝对路径，如"D:/dev/nginx/web/myProject/api-doc"
         */
        configMap.put(apiDocumentPath, "static/api-doc");
    }


    public String getDBPath() {
        return mkCodePath("db");
    }

    public String getApiDocumentPath() {
        String realPath;
        String docPath = configMap.get(apiDocumentPath);
        if (docPath == null || docPath.length() < 2) {
            realPath = basePath + "resources/static/api-doc/";
        } else {
            if (docPath.startsWith("/") || docPath.charAt(1) == ':') {
                realPath = docPath;
            } else {
                realPath = basePath + "resources/" + docPath;
            }
            if (!realPath.endsWith("/")) {
                realPath += "/";
            }
        }
        mkDirs(realPath);
        return realPath;
    }

    public String getDBPackage() {
        return mkPackage(configMap.get(dbPackage));
    }

    public String getEntityPath() {
        return mkCodePath(configMap.get(entityPackage));
    }

    public String getEntityPackage() {
        return mkPackage(configMap.get(entityPackage));
    }

    public String getAccessPath() {
        return mkCodePath(configMap.get(accessPackage));
    }

    public String getAccessPackage() {
        return mkPackage(configMap.get(accessPackage));
    }


    public String getServicePath() {
        return mkCodePath(configMap.get(servicePackage));
    }

    public String getServicePackage() {
        return mkPackage(configMap.get(servicePackage));
    }

    public String getInterfacesPath() {
        return mkCodePath(configMap.get(interfacesPackage));
    }

    public String getInterfacesPackage() {
        return mkPackage(configMap.get(interfacesPackage));
    }

    public String getControllerApiPath() {
        return mkCodePath(configMap.get(apiControllerPackage));
    }

    public String getControllerApiPackage() {
        return mkPackage(configMap.get(apiControllerPackage));
    }

    public String getBasePackage() {
        return basePackage;
    }

    public String getBasePath() {
        return basePath;
    }

    private String mkDirs(String path) {
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
        return path;
    }

    private String mkPackage(String sub) {
        return basePackage + "." + sub;
    }

    private String mkCodePath(String pkg) {
        String path = Strings.replaceAll(pkg, '.', '/') + "/";
        return mkDirs(javaCodePath + path);
    }


}