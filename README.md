# LibertTool

#### 1. 介绍

`LibertTool`是`Libert`库配套的代码生成工具。介绍： http://www.libert.cloud/

> Libert是一个库（`Libert`）和工具（`LibertTool`），也是一套Web开发的约定和规范，囊括了Java Web开发中大部分最佳实践，用于加速基于`SpringBoot`的Web项目开发。在`SpringBoot`强大的框架和生态能力之上，配合`LibertTool`代码生成工具，将极大地提升开发效率。

Libert是一个Java语言实现的Web项目开发库和框架，基于 `JDK1.8`、`JUnit5`、`Maven`构建，提供数据库`ORM`、代码生成、表结构维护的库和工具。方便地和`SpringBoot`，或者传统项目进行集成（可选依赖`@FastJSON`）。

1. 内建数据库脚本管理（目前仅支持`Mysql`，包括建库、建表、`DDL`升级、初始化数据等等），借助`LibertTool`工具，一切脚本即是Java代码。这样的做的好处，对于`java`程序员而言，显而易见——一切尽在自己的掌控之中。
2. 基于`java`实体（`entity`）的`ORM`支持。快速生成数据访问层Access。
3. 基于`java`接口（`interface`），快速建立`Controller`、`Service`，以及接口定义文档（供前端和测试使用）。

#### 2. 基本使用

`LibertTool`提供的功能是根据`entity.java`和`interface.java`生成相应的框架文件。

```java
package com.your-package;

import cloud.libert.tool.LibertTool;
import cloud.libert.tool.LibertToolContext;
import cloud.libert.tool.LibertToolException;

public class LibertToolTest {

    public static void main(String[] args) throws LibertToolException {
        LibertToolContext ctx = new LibertToolContext("D:/dev/java/work/web2020/src/main/", "com.your-package");
        //
        LibertTool tool = new LibertTool(ctx);
        
        //parse entity file: com/your-package/db/entity/KeyValue.java
        tool.addEntity("KeyValue");
        //add any other entities
        
        //parse interface file: com/your-package/service/interfaces/IDataService.java
        tool.addInterface("IDataService");
        //add any other interfaces

        //save the generated files
        tool.save();
    }
}

```

1. `addEntity`添加实体entity文件，`tool.save()`保存后会生成（或更新）以下文件：

   1. 更新~~或新增~~`com/your-package/db/entity/KeyValue.java`文件，添加给每个字段添加注解；
   2. 更新或新增`com/your-package/db/DBInitializer.java`文件，这是向`Mysql`数据库插入初始数据的地方。
   3. 更新或新增`com/your-package/db/DBImpl.java`文件，这是向`Mysql`数据库建表语句的实现。
   4. 更新或新增`com/your-package/db/DBUpgrader.java`文件，这是`Mysql`数据库升级相关实现。前提是`new LibertTool`的时候，第二个参数`upgradeDatabase`传入`true`，否则，默认不新增或更新`DBUpgrader.java`文件。
   5. 更新或新增`com/your-package/db/access/KeyValueAccess.java`文件，这是数据访问层的实现。

2. `addInterface`添加接口文件，`tool.save()`保存后会生成（或更新）以下文件：

   1. 更新或新增`com/your-package/service/DataService.java`。
   2. 更新或新增`com/your-package/controller/api/DataController.java`。
   3. 新增接口描述文档：`resources/static/api-doc/DataService.md`。

   