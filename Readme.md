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

**解决遗留问题：**

昨天写到需要读取xml配置，并通过dtd进行文件校验，在读取到publicId和systemId后通过dtd校验时，我发获取到资源文件com/jingxc/ibatis/builder/xml/mybatis-3-config.dtd

**原因所在：**

java在运行代码时，需要先将资源文件编译进classpath路径下，在从classpath中获取资源文件以及代码运行，但是springboot默认是从src/main/resources中读取，所以为加载到mybatis-3-config.dtd文件

**解决办法：**

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

#### XML文件构建

构建SqlSessionFactory是通过构建者SqlSessionFactoryBuilder构建的，SqlSessionFactoryBuilder首先构建了一个XML文挡构建者，
这里体现了多次使用构建者模式以减少类创建的复杂程度，由于代码还未完善暂不贴出全部代码

##### TEST

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

##### SqlSessionFactoryBuilder

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

##### XMLConfigBuilder

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

##### 解析器：XPathParser

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

### 20231110

**昨天写到：** 通过XMLConfigBuilder构建者的构造器创建完成XPathParser解析器，调用XMLConfigBuilder构建者的parse方法即可封装获取XNode节点，进而封装全剧配置类Configuration
**后续完成：** 已经拿到XPathParser解析器，接下来就可以通过解析器XPathParser的evalNode方法获取Node节点，并封装成ibatis的
XNode节点，在封装XNode过程中已经处理了变量描述符

#### XMLConfigBuilder构建者的parse方法

```java
public class XMLConfigBuilder extends BaseBuilder {

    private boolean parsed;

    // XPath解析器
    private final XPathParser xPathParser;

    private String environment;

    public XMLConfigBuilder(InputStream inputStream, String environment, Properties props) {
        // XPathParser基于Java XPath解析器，用于解析Mybatis配置文件
        this(new XPathParser(inputStream, true, props, new XMLMapperEntityResolver()), environment, props);
    }

    /**
     * 构造器
     *
     * @param xPathParser
     * @param environment
     * @param props
     */
    private XMLConfigBuilder(XPathParser xPathParser, String environment, Properties props) {
        // 在父抽象类中定义（父类属性）
        // 创建Configuration对象，并通过TypeAliasRegistry注册一些Mybatis内部相关类的别名
        super(new Configuration());

        // 在MyBatis的源代码中，ErrorContext.instance().resource("SQL Mapper Configuration")这行代码用于创建一个ErrorContext的实例，并将资源名称设置为"SQL Mapper Configuration"。

        // ErrorContext是MyBatis中的一个类，用于处理错误信息。它主要用于记录错误的上下文信息，包括发生错误的位置、错误的原因等。通过使用ErrorContext.instance().resource("SQL Mapper Configuration")，可以创建一个错误上下文实例，并将当前错误的资源名称设置为"SQL Mapper Configuration"。

        // 这个资源名称通常是指MyBatis的映射配置文件，例如Mapper XML文件。当MyBatis在处理映射配置文件时出现错误，就会使用这个资源名称来标识错误的来源。这样，在打印错误信息时，可以更清晰地知道错误发生在哪个映射配置文件上，方便开发者进行调试和排查问题。

        // 总结起来，这行代码的作用是创建一个错误上下文实例，并将当前错误的资源名称设置为"SQL Mapper Configuration"，以便在后续处理中能够更准确地标识错误的来源。
        // ErrorContext.instance().resource("SQL Mapper Configuration");

        // 设置属性集
        this.configuration.setVariables(props);

        // 执行标记
        this.parsed = false;
        // XPath解析器
        this.xPathParser = xPathParser;
        this.environment = environment;
    }

    public Configuration parse() {
        if (parsed) {
            throw new RuntimeException("每个XMLConfigBuilder构建者只能被创建一次");
        }
        parsed = true;

        // 解析配置文件初始化Configuration
        XNode xNode = xPathParser.evalNode("/configuration");
        XNode environments = xNode.evalNode("environments");

        System.out.println(environments.toString());
        return null;
    }
}
```

这里已经解析到了XNode xNode = xPathParser.evalNode("/configuration")，后续在调用相关方法，即可完成全局配置文件Configuration的创建
在解析XNode的过程中调用了evalNode方法

#### 解析器XPathParser的evalNode方法

```java
public class XPathParser {

    private boolean validation;
    // 使 XML 加载的过程中不需要通过网络下载约束文件。这种情况下，通过EntityResolver告诉解析器如何找到正确的约束文件
    private EntityResolver entityResolver;

    // 属性集
    private Properties variables;

    // XPath是一种在XML文档中定位节点的语言，它可以根据节点的属性、元素名称等条件来进行查询。在Java中，可以使用XPath来操作XML文档，实现对特定节点的查找、遍历和修改等操作
    private XPath xpath;

    // 用于描述HTML或XML文档。它提供了许多方法，可以获取文档的信息，其中包括文档的标题、元素、属性等等。使用Document类，我们可以方便地获取HTML或XML文档中的信息，以便我们对文档进行各种操作。
    private final Document document;

    public XPathParser(InputStream inputStream, boolean validation, Properties variables, EntityResolver entityResolver) {
        // 前面已经完成，此处省略....
    }

    private void commonConstructor(boolean validation, Properties variables, EntityResolver entityResolver) {
        // 前面已经完成，此处省略....
    }

    /**
     * 根据节点名称expression获取XNode节点
     *
     * @param expression
     * @return
     */
    public XNode evalNode(String expression) {
        return evalNode(document, expression);
    }

    /**
     * 根具节点名称expression，从Document对象中获取XNode对像
     *
     * @param root
     * @param expression
     * @return
     */
    public XNode evalNode(Object root, String expression) {
        // 解析Node对象
        Node node = (Node) evaluate(expression, root, XPathConstants.NODE);
        if (null == node) {
            return null;
        }
        // 封装并返回ibatis封装的XNode对，这里已经对变量描述符做了调整，具体在GenericTokenParser中
        return new XNode(this, node, variables);
    }

    /**
     * 从root对象中解析expression对应标签，返回returnType实体对象
     *
     * @param expression
     * @param root
     * @param returnType
     * @return
     */
    private Object evaluate(String expression, Object root, QName returnType) {
        try {
            // 解析标签返回对象jdk方法
            return xpath.evaluate(expression, root, returnType);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("构建xpath出错.  原因: " + e, e);
        }
    }

    private Document createDocument(InputSource inputSource) {
        // 前面已经完成，此处省略....
    }
}
```

在evalNode方法中主要是将Document对象中的Node节点封装成XNode节点

```java
public class XNode {

    // XPath解析器对象
    private final XPathParser xPathParser;

    // Node节点对象
    private final Node node;

    // 属性集
    private final Properties variables;

    //  Node(配置文件)节点属性集
    private final Properties attributes;

    // 节点名称
    private final String name;

    private final String body;

    public XNode(XPathParser xPathParser, Node node, Properties variables) {
        // 解析器
        this.xPathParser = xPathParser;
        // 节点
        this.node = node;
        this.name = node.getNodeName();
        // 属性集
        this.variables = variables;

        // Node节点属性集
        this.attributes = parseAttribute(node);

        this.body = parseBody(node);

    }

    private String parseBody(Node node) {
        // 获取第一个不是null的bodyData
        String data = getBodyData(node);
        if (data == null) {
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);
                data = getBodyData(item);
                if (data != null) {
                    break;
                }

            }
        }
        return data;
    }

    private String getBodyData(Node node) {
        if (node.getNodeType() == Node.CDATA_SECTION_NODE || node.getNodeType() == Node.TEXT_NODE) {
            String data = ((CharacterData) node).getData();
            data = PropertyParser.parse(data, variables);
            return data;
        }
        return null;
    }

    /**
     * 获取并创建Node节点属性集
     *
     * @param node
     * @return
     */
    private Properties parseAttribute(Node node) {
        // 创建属性集
        Properties properties = new Properties();
        // 获取节点属性集
        NamedNodeMap nameNodeMap = node.getAttributes();
        if (nameNodeMap != null) {
            // 循环node节点属性封装Properties属性集
            for (int i = 0; i < nameNodeMap.getLength(); i++) {
                Node n = nameNodeMap.item(i);
                // 获取节点属性值，并处理变量描述符
                String value = PropertyParser.parse(n.getNodeValue(), variables);

                // 节点属性集赋值
                properties.put(n.getNodeName(), value);
            }
        }
        return properties;
    }

    public XNode evalNode(String expression) {
        return xPathParser.evalNode(node, expression);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        toString(builder, 0);
        return builder.toString();
    }

    private void toString(StringBuilder builder, int level) {
        builder.append("<");
        builder.append(name);
        for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
            builder.append(" ");
            builder.append(entry.getKey());
            builder.append("=\"");
            builder.append(entry.getValue());
            builder.append("\"");
        }
        List<XNode> children = getChildren();
        if (!children.isEmpty()) {
            builder.append(">\n");
            for (XNode child : children) {
                indent(builder, level + 1);
                child.toString(builder, level + 1);
            }
            indent(builder, level);
            builder.append("</");
            builder.append(name);
            builder.append(">");
        } else if (body != null) {
            builder.append(">");
            builder.append(body);
            builder.append("</");
            builder.append(name);
            builder.append(">");
        } else {
            builder.append("/>");
            indent(builder, level);
        }
        builder.append("\n");
    }

    private void indent(StringBuilder builder, int level) {
        for (int i = 0; i < level; i++) {
            builder.append("    ");
        }
    }

    public List<XNode> getChildren() {
        List<XNode> children = new ArrayList<>();
        NodeList nodeList = node.getChildNodes();
        if (nodeList != null) {
            for (int i = 0, n = nodeList.getLength(); i < n; i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    children.add(new XNode(xPathParser, node, variables));
                }
            }
        }
        return children;
    }
}
```

在XNode类中主要是对节点封装，并在PropertyParser.parse方法中处理了变量描述符

#### PropertyParser

```java
public class PropertyParser {

    private static final String KEY_PREFIX = "com.jingxc.ibatis.parsing.PropertyParser.";

    public static final String KEY_ENABLE_DEFAULT_VALUE = KEY_PREFIX + "enable-default-value";

    public static final String KEY_DEFAULT_VALUE_SEPARATOR = KEY_PREFIX + "default-value-separator";

    private static final String ENABLE_DEFAULT_VALUE = "false";

    private static final String DEFAULT_VALUE_SEPARATOR = ":";

    /**
     * 对比获取属性值(默认值，传入值)
     *
     * @param nodeValue
     * @param variables
     * @return
     */
    public static String parse(String nodeValue, Properties variables) {
        // 拦截器
        VariableTokenHandler h = new VariableTokenHandler(variables);
        // 去除变量描述字符
        GenericTokenParser genericTokenParser = new GenericTokenParser("${", "}", h);

        return genericTokenParser.parse(nodeValue);
    }

    private static class VariableTokenHandler implements TokenHandler {

        private final Properties variables;
        private final boolean enableDefaultValue;

        private final String defaultValueSeparator;

        public VariableTokenHandler(Properties variables) {
            this.variables = variables;
            this.enableDefaultValue = Boolean.parseBoolean(getPropertyValue(KEY_ENABLE_DEFAULT_VALUE, ENABLE_DEFAULT_VALUE));
            this.defaultValueSeparator = getPropertyValue(KEY_DEFAULT_VALUE_SEPARATOR, DEFAULT_VALUE_SEPARATOR);
        }

        private String getPropertyValue(String key, String defaultValue) {
            return variables == null ? defaultValue : variables.getProperty(key, defaultValue);
        }

        @Override
        public String handleToken(String content) {
            return null;
        }
    }
}
```

#### GenericTokenParser

```java
public class GenericTokenParser {

    private final String openToken;
    private final String closeToken;
    private final TokenHandler handler;

    public GenericTokenParser(String openToken, String closeToken, TokenHandler h) {
        this.openToken = openToken;
        this.closeToken = closeToken;
        this.handler = h;
    }

    public String parse(String text) {
        if (null == text) {
            return "";
        }
        int start = text.indexOf(openToken);

        // 如果text中不包含openToken,则直接返回，否则需要预处理，mysql中变量${}
        if (start == -1) {
            return text;
        }

        // 转换为字符集
        char[] src = text.toCharArray();
        // 偏移量
        int offset = 0;

        final StringBuilder builder = new StringBuilder();
        StringBuilder expression = null;
        do {
            if (start > 0 && src[start - 1] == '\\') {
                // 删除openToken前的转译字符，如\${==>${
                builder.append(src, offset, start - offset - 1).append(openToken);
                // 使偏移量移至openToken后面
                offset = start + openToken.length();
            } else {
                if (expression == null) {
                    expression = new StringBuilder();
                } else {
                    expression.setLength(0);
                }

                builder.append(src, offset, start - offset);
                offset = start + openToken.length();

                // 返回此字符串中第一次出现指定子字符串的索引，从指定索引开始。
                // 处理完openToken，接着处理closeToken
                int end = text.indexOf(closeToken, offset);
                while (end > -1) {
                    if (end > offset && src[end - 1] == '\\') {
                        // 仅仅删除转义字符
                        expression.append(src, offset, end - offset - 1).append(closeToken);
                        offset = end + closeToken.length();
                        // 获取end的相对位置不变，只是为了去除转译字符，用于与openToken配对只能是，text的第一个
                        end = text.indexOf(closeToken, offset);
                    } else {
                        // 遇见配对的直接结束循环
                        expression.append(src, offset, end - offset);
                        break;
                    }
                }
                if (end == -1) {
                    // 没有发现closeToken
                    builder.append(src, start, src.length - start);
                    offset = src.length;
                } else {
                    // 发现closeToken，拼接在 while (end > -1)循环中存储的中间字符
                    builder.append(handler.handleToken(expression.toString()));
                    offset = end + closeToken.length();
                }
            }
            start = text.indexOf(openToken, offset);
        } while (start > -1);
        if (offset < src.length) {
            builder.append(src, offset, src.length - offset);
        }
        return builder.toString();
    }
}
```