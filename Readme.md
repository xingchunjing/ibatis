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

#### maven依赖

添加测试文件，引入第一个maven依赖

```xml

<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.13.2</version>
    <scope>test</scope>
</dependency>
```

测试文件路径：/src/test/java/com/jingxc/ibatis/test/IbatisTest.java

```java
public class IbatisTest {
    @Test
    public void test1() throws IOException {

        // 1.通过类加载器对配置文件进行加载，加载成字节数入流，存到内存中，注意：配置文件并没有被解析
        InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
        System.out.println(resourceAsStream);
    }
}
```

* 通过类加载器对配置文件进行加载，加载成字节数入流，存到内存中，注意：配置文件并没有被解析

类加载器路径：src/main/java/com/jingxc/ibatis/io/Resources.java

```java
public class Resources {

    // 类加载器包装类
    private static ClassLoaderWrapper classLoaderWrapper = new ClassLoaderWrapper();

    /**
     * 加载文件，将类路径上的资源作为流对象返回
     *
     * @param resource
     * @return
     * @throws IOException
     */
    public static InputStream getResourceAsStream(String resource) throws IOException {
        return getResourceAsStream(null, resource);
    }

    /**
     * 加载文件，将类路径上的资源作为流对象返回
     *
     * @param loader
     * @param resource
     * @return
     * @throws IOException
     */
    public static InputStream getResourceAsStream(ClassLoader loader, String resource) throws IOException {
        InputStream in = classLoaderWrapper.getResourceAsStream(resource, loader);
        if (in == null)
            throw new IOException("未发现资源文件 " + resource);
        return in;
    }
}
```

* 这里有个类加载器的包装类：用于包装对多个类加载器的访问，使它们作为一个类工作

类加载器的包装类路径：src/main/java/com/jingxc/ibatis/io/ClassLoaderWrapper.java

```java
public class ClassLoaderWrapper {

    // 默认类加载器
    ClassLoader defaultClassLoader;

    // 系统类加载器
    ClassLoader systemClassLoader;

    ClassLoaderWrapper() {
        try {
            systemClassLoader = ClassLoader.getSystemClassLoader();
        } catch (SecurityException ignored) {

        }
    }

    /**
     * 通过特定的类加载器，从资源路径获取文件流
     *
     * @param resource
     * @param loader
     * @return
     */
    public InputStream getResourceAsStream(String resource, ClassLoader loader) {
        return getResourceAsStream(resource, getClassLoaders(loader));
    }

    /**
     * 从一组类加载器中获取资源
     *
     * @param resource
     * @param classLoaders
     * @return
     */
    InputStream getResourceAsStream(String resource, ClassLoader[] classLoaders) {
        // 训话类加载器集合
        for (ClassLoader cl : classLoaders) {
            if (cl != null) {
                // 尝试获取资源
                InputStream value = cl.getResourceAsStream(resource);
                if (null == value) {
                    value = cl.getResourceAsStream("/" + resource);
                }

                if (null != value) {
                    return value;
                }
            }
        }
        return null;
    }

    ClassLoader[] getClassLoaders(ClassLoader classLoader) {
        return new ClassLoader[]{
                // 参数指定类加载器
                classLoader,
                // 默认类加载器
                defaultClassLoader,
                // 当前线程绑定的类加载器
                Thread.currentThread().getContextClassLoader(),
                // 当前类使用的类加载器
                getClass().getClassLoader(),
                // 系统类加载器
                systemClassLoader
        };
    }
}
```

* <font color=red>多次使用重载<font/>


