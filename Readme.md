## 学习记录

### 20231108

完成配置文件的加载

sqlMapConfig.xml文件中配置的：

```xml
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd"
        >
<configuration/>
```

* 其中的http://mybatis.org/dtd/mybatis-3-config.dtd可以直接指向包文件中的dtd？
* 该文件限制了配置文件的配置顺序，以及标签格式,用于验证配置文件的合法性

#### 配置文件

在test目录中新建源文件resources，创建配置文件sqlMapConfig.xml

配置文件路径：src/test/resources/sqlMapConfig.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd"
        >
<configuration>

    <!-- 数据源配置 -->
    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"></transactionManager>
            <dataSource type="POOLED">
                <property name="driver" value="com.mysql.cj.jdbc.Driver"/>
                <property name="url" value="jdbc:mysql://127.0.0.1:3306/test_demo_0"/>
                <property name="username" value="root"/>
                <property name="password" value="100uu100UU"/>
            </dataSource>
        </environment>
    </environments>

    <!-- 引入配置文件 -->
    <mappers>
        <mapper resource="mapper/UserMapper.xml"></mapper>
    </mappers>

</configuration>
```

添加测试文件，引入第一个maven依赖

测试文件路径：/src/test/java/com/jingxc/ibatis/test/IbatisTest.java

```xml

<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.13.2</version>
    <scope>test</scope>
</dependency>
```

通过类加载器对配置文件进行加载，加载成字节数入流，存到内存中，注意：配置文件并没有被解析

类加载器路径：src/main/java/com/jingxc/ibatis/io/Resources.java

这里有个类加载器的包装类：用于包装对多个类加载器的访问，使它们作为一个类工作

类加载器的包装类路径：src/main/java/com/jingxc/ibatis/io/ClassLoaderWrapper.java

<font color=red>多次使用重载<font/>


