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

* 多次使用重载

今日完成：已完成dtd文件的读取对xml文件进行校验

今日遗留：

```text
java.io.IOException: 未发现资源文件 com/jingxc/ibatis/builder/xml/mybatis-3-config.dtd
	at com.jingxc.ibatis.io.Resources.getResourceAsStream(Resources.java:33)
	at com.jingxc.ibatis.io.Resources.getResourceAsStream(Resources.java:19)
```

### 20231109

解决遗留问题：昨天写到需要读取xml配置，并通过dtd进行文件校验，在读取到publicId和systemId后通过dtd校验时，我发获取到资源文件com/jingxc/ibatis/builder/xml/mybatis-3-config.dtd

原因所在：java在运行代码时，需要先将资源文件编译进classpath路径下，在从classpath中获取资源文件以及代码运行，但是springboot默认是从src/main/resources中读取，所以为加载到mybatis-3-config.dtd文件

解决办法：

* 将资源文件都放在src/main/resources中
* 添加配置单独引入资源文件,在pom文件中添加

```xml

<build>
    <resources>
        <resource>
            <directory>src/main/java</directory>
            <includes>
                <include>**/*.dtd</include>
            </includes>
        </resource>
    </resources>
</build>
```

#### XML处理

自定义外部处理xml实体，需要实现EntityResolver接口，然后实现resolveEntity方法,以便通过本地资源文件文件对xml进行验证

```text
对于解析一个xml,sax
首先会读取该xml文档上的声明,根据声明去寻找相应的dtd定义,以便对文档的进行验证,
默认的寻找规则,(即:通过网络,实现上就是声明DTD的地址URI地址来下载DTD声明),
并进行认证,下载的过程是一个漫长的过程,而且当网络不可用时,这里会报错,就是因为相应的dtd没找到
```

XML验证文件路径：com/jingxc/ibatis/builder/xml/XMLMapperEntityResolver.java

```java
/**
 * 如果ＳＡＸ应用程序实现自定义处理外部实体,则必须实现此接口EntityResolver,
 * 并使用setEntityResolver方法向SAX 驱动器注册一个实例
 */
public class XMLMapperEntityResolver implements EntityResolver {

    private static final String IBATIS_CONFIG_SYSTEM = "ibatis-3-config.dtd";
    private static final String MYBATIS_CONFIG_SYSTEM = "mybatis-3-config.dtd";

    private static final String MYBATIS_MAPPER_SYSTEM = "mybatis-3-mapper.dtd";

    private static final String IBATIS_MAPPER_SYSTEM = "ibatis-3-mapper.dtd";

    private static final String MYBATIS_CONFIG_DTD = "com/jingxc/ibatis/builder/xml/mybatis-3-config.dtd";
    private static final String MYBATIS_MAPPER_DTD = "com/jingxc/ibatis/builder/xml/mybatis-3-mapper.dtd";

    /**
     * 对于解析一个xml,sax
     * 首先会读取该xml文档上的声明,根据声明去寻找相应的dtd定义,以便对文档的进行验证,
     * 默认的寻找规则,(即:通过网络,实现上就是声明DTD的地址URI地址来下载DTD声明),
     * 并进行认证,下载的过程是一个漫长的过程,而且当网络不可用时,这里会报错,就是因为相应的dtd没找到
     *
     * @param publicId The public identifier of the external entity
     *                 being referenced, or null if none was supplied.
     * @param systemId The system identifier of the external entity
     *                 being referenced.
     * @return
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
        // EntityResolver 的作用就是项目本身就可以提供一个如何寻找DTD 的声明方法,
        // 即:由程序来实现寻找DTD声明的过程,我们将DTD放在项目的某处在实现时直接将此文档读取并返回个SAX即可,这样就避免了通过网络来寻找DTD的声明
        try {
            if (systemId != null) {
                String lowerCaseSystemId = systemId.toLowerCase(Locale.ENGLISH);
                if (lowerCaseSystemId.contains(MYBATIS_CONFIG_SYSTEM) || lowerCaseSystemId.contains(IBATIS_CONFIG_SYSTEM)) {
                    return getInputSource(MYBATIS_CONFIG_DTD, publicId, systemId);
                } else if (lowerCaseSystemId.contains(MYBATIS_MAPPER_SYSTEM) || lowerCaseSystemId.contains(IBATIS_MAPPER_SYSTEM)) {
                    return getInputSource(MYBATIS_MAPPER_DTD, publicId, systemId);
                }
            }
            return null;
        } catch (Exception e) {
            throw new SAXException(e.toString());
        }
    }

    /**
     * 具体的声明方法
     *
     * @param path
     * @param publicId
     * @param systemId
     * @return
     */
    private InputSource getInputSource(String path, String publicId, String systemId) {
        InputSource source = null;
        if (path != null) {
            try {
                InputStream in = Resources.getResourceAsStream(path);
                source = new InputSource(in);
                source.setSystemId(systemId);
                source.setPublicId(publicId);
            } catch (IOException ignore) {
                ignore.printStackTrace();
            }
        }
        return source;
    }
}
```

### XML文件构建

构建SqlSessionFactory是通过构建者SqlSessionFactoryBuilder构建的，SqlSessionFactoryBuilder首先构建了一个XML文挡构建者，
这里体现了多次使用构建者模式以减少类创建的复杂程度，由于代码还未完善暂不贴出全部代码

#### TEST

```java
public class IbatisTest {
    @Test
    public void test1() throws IOException {
        // 1.通过类加载器对配置文件进行加载，加载成字节数入流，存到内存中，注意：配置文件并没有被解析
        InputStream inputStream = Resources.getResourceAsStream("sqlMapConfig.xml");

        // 通过构建者模式，构建SqlSessionFactory工厂
        SqlSessionFactory build = new SqlSessionFactoryBuilder().build(inputStream);
        System.out.println(inputStream);
    }
}
```

#### SqlSessionFactoryBuilder

```java
public class SqlSessionFactoryBuilder {

    /**
     * 直接通过文件流，构建session工厂
     *
     * @param inputStream
     * @return
     */
    public SqlSessionFactory build(InputStream inputStream) {
        return build(inputStream, null, null);
    }

    public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
        // XMLConfigBuilder：用来解析xml配置文件
        // 使用构建者模式，降低耦合，分离复杂对象的创建
        // 1.创建XPathParser解析器对象，根据inputStream解析成Document对象; 2.创建全剧配置对象Configuration
        XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder(inputStream, environment, properties);
        return null;
    }
}
```

#### XMLConfigBuilder

```java
public class XMLConfigBuilder {
    public XMLConfigBuilder(InputStream inputStream, String environment, Properties props) {
        // XPathParser基于Java XPath解析器，用于解析Mybatis配置文件
        this(new XPathParser(inputStream, true, props, new XMLMapperEntityResolver()), environment, props);
    }

    private XMLConfigBuilder(XPathParser parser, String environment, Properties props) {

    }
}
```

#### 解析器：XPathParser

```java
public class XPathParser {

    private boolean validation;
    // 使 XML 加载的过程中不需要通过网络下载约束文件。这种情况下，通过EntityResolver告诉解析器如何找到正确的约束文件
    private EntityResolver entityResolver;
    private Properties variables;

    // XPath是一种在XML文档中定位节点的语言，它可以根据节点的属性、元素名称等条件来进行查询。在Java中，可以使用XPath来操作XML文档，实现对特定节点的查找、遍历和修改等操作
    private XPath xpath;

    private final Document document;

    public XPathParser(InputStream inputStream, boolean validation, Properties variables, EntityResolver entityResolver) {
        commonConstructor(validation, variables, entityResolver);
        this.document = createDocument(new InputSource(inputStream));
    }

    private Document createDocument(InputSource inputSource) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setValidating(validation);
            factory.setNamespaceAware(false);
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(false);
            factory.setCoalescing(false);
            factory.setExpandEntityReferences(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(entityResolver);

            builder.setErrorHandler(new ErrorHandler() {
                @Override
                public void error(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    // NOP
                }
            });
            return builder.parse(inputSource);
        } catch (Exception e) {
            throw new RuntimeException("创建document出错.  原因: " + e, e);
        }

    }

    private void commonConstructor(boolean validation, Properties variables, EntityResolver entityResolver) {
        this.validation = validation;
        this.entityResolver = entityResolver;
        this.variables = variables;

        // 创建XPath对象
        XPathFactory xPathFactory = XPathFactory.newInstance();
        this.xpath = xPathFactory.newXPath();
    }
}
```

这就完成了XPathParser解析器的创建，接下来就可以就可以创建并解析全局配置文件Configuration

