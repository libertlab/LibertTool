# LibertTool

### 1. 介绍

`LibertTool`是`Libert`框架配套的开发辅助工具（含有代码生成、接口文档生成、部署）。

网址（Sorry，网站正在筹备中，暂时无法访问...）： http://www.libert.cloud/

> Libert是一个库（`Libert`）和工具（`LibertTool`），也是一套Web开发的约定和规范，囊括了Java Web开发中大部分最佳实践，用于加速基于`SpringBoot`的Web项目开发。在`SpringBoot`强大的框架和生态能力之上，配合`LibertTool`代码生成工具，将极大地提升开发效率。

Libert是一个Java语言实现的Web项目开发库和框架，基于 `JDK1.8`、`JUnit5`、`Maven`构建，提供数据库`ORM`、代码生成、表结构维护的库和工具。方便地和`SpringBoot`，或者传统项目进行集成（可选依赖`@FastJSON`）。

1. 内建数据库脚本管理（目前仅支持`Mysql`，包括建库、建表、`DDL`升级、初始化数据等等），借助`LibertTool`工具，一切脚本即是Java代码。这样的做的好处，对于`java`程序员而言，显而易见——一切尽在自己的掌控之中。
2. 基于`java`实体（`entity`）的`ORM`支持。快速生成数据访问层Access。
3. 基于`java`接口（`interface`），快速建立符合`SpringBoot`规范的`Controller`、`Service`，以及接口定义文档（供前端和测试使用）。
4. **在`Libert`库和`LibertTool`工具的支持下，基于`SpringBoot`的服务开发将变得简单、高效、极速**。

### 2. 功能清单

1. `Mysql`数据库脚本管理

2. 实体所对应的`Access.java`文件生成

3. 接口所对应的`Controller/Service`文件生成

4. 接口文档生成

5. **（新）增加Markdown解析器，支持Markdown转特定格式的HTML**。

   ```java
   public static void main(String[] args) throws IOException {
       File f = new File("D:/test/LibertTool/test.md");
       try {
           Markdown mp = new Markdown(f);
           mp.writeTo("D:/test/LibertTool/test_out.md");
           //Markdown转html
           System.out.println(mp.toHtml());
       } catch (LibertToolException e) {
           e.printStackTrace();
       }
   }
   ```

### 3. 示例

以`KeyValue.java`实体和`IDataService.java`接口为例。

```java
//原始的KeyValue.java
public class KeyValue {
    public long id;
    @dbmap(length=127)
    public String key;
    public String value;
    public String description;
}
//原始的IDataService.java
public interface IDataService {
    /**
     * 保存实体
     * @param entityName 实体类型名称
     * @param jsonStr json序列化后的实体对象
     * @return
     * @throws OperatorException
     */
    public long save(String entityName, String jsonStr) throws OperatorException;
    public void update(String entityName, String jsonStr) throws OperatorException;
}
```

1. 工具修改后的`KeyValue.java`：

```java
public class KeyValue {
    @JSONField(name="id") 
    public long id;
    @JSONField(name="k") 
    public String key;
    @JSONField(name="v") 
    public String value;
    @JSONField(name="d") 
    public String description;
}
```

1. 根据`KeyValue.java`实体生成的数据库建表语句：

```java
//位于DBImpl.java
protected void createTableKeyValue(Statement stat) throws SQLException {
    TableBuilder tb = new TableBuilder("key_value");
    tb.add("_key", typeString(127), null);
    tb.add("_value", typeString(), null);
    tb.add("_description", typeString(), null);
    stat.executeUpdate(tb.build());
}
```

2. 根据`KeyValue.java`实体生成的`KeyValueAccess.java`文件：

```java
//继承Libert库中的cloud.libert.database.BaseAccess
public class KeyValueAccess extends BaseAccess<KeyValue> {
    private static volatile KeyValueAccess selfInstance;
    public static final String col_id = "_id";
    public static final String col_key = "_key";
    public static final String col_value = "_value";
    public static final String col_description = "_description";

    private KeyValueAccess() {
        entityName = "KeyValue";
        tableName = "key_value";
        entityClazz = KeyValue.class;
        fieldsMap.put(col_key, "key");
        fieldsMap.put(col_value, "value");
        fieldsMap.put(col_description, "description");
        init();
    }
    public static KeyValueAccess self() {
		if (selfInstance == null) {
		    synchronized (KeyValueAccess.class) {
                if (selfInstance == null) {
                    selfInstance = new KeyValueAccess();
                }
            }
		}
		return selfInstance;
    }
    private void init() {
    }
}
```

其中，`BaseAccess`实现了如下接口：

```java
public interface IAccess {
    public static String ENTITY_ID = "id";
    public static String TABLE_ID = "_id";

    //查询条件链式拼接
    public Query Q();
    public Class<?> getEntityClazz();
    public String getTableName();
    public Map<String, String> getFieldsMap();

    public Object one(String where) throws OperatorException;
    public List<?> all(String where) throws OperatorException;
    public long saveEntity(Object entity) throws OperatorException;
    public void updateEntity(Object entity) throws OperatorException;
    public List<?> getByPage(int pageNum, int pageSize, String where) throws OperatorException;
    public int getCount(String where) throws OperatorException;
    public void notifyUpdated(long entityId) throws OperatorException;
    public void notifySaved(long entityId) throws OperatorException;
}

```

因此`KeyValueAccess`具备了`DAO`的功能。此外，**`Libert`库通过`Query`和`JoinQuery`两个类，以精心设计的链式书写的方式，提供了一部分复杂查询的能力**。

3. 根据`IDataService.java`接口文件生成的Service实现：

```java
@Service
public class DataService implements IDataService {
    private static HashMap<String, Supplier<IAccess>> accessWare = new HashMap<>();

    public DataService() {
        accessWare.put("KeyValue", () -> CaseIndustryAccess.self());
   		//...
    }

    @Override
    public long save(String entityName, String jsonStr) throws OperatorException {
        Supplier<IAccess> supplier = accessWare.get(entityName);
        long rc = 0;
        if (supplier != null) {
            IAccess access = supplier.get();
            Object obj = JSON.parseObject(jsonStr, access.getEntityClazz());
            rc = access.saveEntity(obj);
        }
        return rc;
    }

    @Override
    public void update(String entityName, String jsonStr) throws OperatorException {
        Supplier<IAccess> supplier = accessWare.get(entityName);
        if (supplier != null) {
            IAccess access = supplier.get();
            Object obj = JSON.parseObject(jsonStr, access.getEntityClazz());
            access.updateEntity(obj);
        }
    }
}
```

4. 根据`IDataService.java`接口文件生成的Controller实现

```java
@RestController
@RequestMapping("/api/data")
public class DataController {
    private final Log logger = LogFactory.getLog(getClass());
    private final DataService service;

    @Autowired
    public DataController(DataService service) {
        this.service = service;
        init();
    }

    private void init() {
    }

    @PostMapping("/save")
    public Response save(@RequestParam("en") String entityName, 
                         @RequestParam("js") String jsonStr) throws OperatorException {
        try {
            return Response.ok().putData(service.save(entityName, jsonStr));
        } catch (OperatorException e) {
            e.printStackTrace();
            return Response.of(e.statusCode());
        }
    }

    @PostMapping("/update")
    public Response update(@RequestParam("en") String entityName, 
                           @RequestParam("js") String jsonStr) throws OperatorException {
        try {
            service.update(entityName, jsonStr);
            return Response.ok();
        } catch (OperatorException e) {
            e.printStackTrace();
            return Response.of(e.statusCode());
        }
    }
}
```

没错，这就是标准的`SpringMVC/SpringBoot`下的Controller。

### 3. LibertTool使用

`LibertTool`提供的核心功能是根据`entity.java`和`interface.java`生成相应的框架文件。

```java
package com.your-package;

import cloud.libert.tool.LibertTool;
import cloud.libert.tool.LibertToolContext;
import cloud.libert.tool.LibertToolException;

public class LibertToolTest {

    public static void main(String[] args) throws LibertToolException {
        //设置项目的源码目录和包名
        LibertToolContext ctx = new LibertToolContext("D:/dev/java/work/web2020/src/main/", "com.your-package");
        //
        LibertTool tool = new LibertTool(ctx);
        
        //parse entity file: com/your-package/db/entity/KeyValue.java
        tool.addEntity("KeyValue");
        //...add any other entities
        
        //parse interface file: com/your-package/service/interfaces/IDataService.java
        tool.addInterface("IDataService");
        //...add any other interfaces

        //save the generated files
        tool.save();
    }
}

```

1. `addEntity`添加实体entity文件，`tool.save()`保存后会生成（或更新）以下文件：

   1. 更新~~或新增~~`com/your-package/db/entity/KeyValue.java`文件，添加给每个字段添加`@JSONField`注解；
   2. 更新或新增`com/your-package/db/DBInitializer.java`文件，这是向`Mysql`数据库插入初始数据的地方。
   3. 更新或新增`com/your-package/db/DBImpl.java`文件，这是向`Mysql`数据库建表语句的实现。
   4. 更新或新增`com/your-package/db/DBUpgrader.java`文件，这是`Mysql`数据库升级相关实现。**前提是`new LibertTool`的时候，第二个参数`upgradeDatabase`传入`true`，否则，默认不新增或更新`DBUpgrader.java`文件**。
   5. 更新或新增`com/your-package/db/access/KeyValueAccess.java`文件，这是数据访问层的实现。

2. `addInterface`添加接口文件，`tool.save()`保存后会生成（或更新）以下文件：

   1. 更新或新增`com/your-package/service/DataService.java`。
   2. 更新或新增`com/your-package/controller/api/DataController.java`。
   3. 新增接口描述文档：`resources/static/api-doc/DataService.md`。**前提是`new LibertTool`的时候，第三个参数`createDocument`传入`true`，否则，默认不创建接口文档**。
