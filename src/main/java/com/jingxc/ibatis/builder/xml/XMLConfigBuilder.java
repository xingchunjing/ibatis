package com.jingxc.ibatis.builder.xml;

import com.jingxc.ibatis.builder.BaseBuilder;
import com.jingxc.ibatis.io.Resources;
import com.jingxc.ibatis.io.VFS;
import com.jingxc.ibatis.logging.Log;
import com.jingxc.ibatis.parsing.XNode;
import com.jingxc.ibatis.parsing.XPathParser;
import com.jingxc.ibatis.reflection.DefaultReflectionFactory;
import com.jingxc.ibatis.reflection.MetaClass;
import com.jingxc.ibatis.reflection.ReflectionFactory;
import com.jingxc.ibatis.session.Configuration;

import java.io.InputStream;
import java.util.Properties;

public class XMLConfigBuilder extends BaseBuilder {

    private boolean parsed;

    // XPath解析器
    private final XPathParser xPathParser;

    private String environment;

    // 反射工厂
    private ReflectionFactory localReflectorFactory = new DefaultReflectionFactory();

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
        System.out.println(xNode.toString());

        // 从根结点开始解析并构建Configuration对象
        parseConfiguration(xNode);

        return configuration;
    }

    /**
     * 解析XNode，构建Configuration对象
     *
     * @param xNode
     */
    private void parseConfiguration(XNode xNode) {
        try {
            // 属性集设置，主要解析在配置文件中的<properties></properties>
            // 属性集可以在标签中直接配置，也可以通过配置文件传入，例
            /**
             * <properties resource="org/mybatis/example/config.properties">
             *   <property name="username" value="dev_user"/>
             *   <property name="password" value="F2Fa3!33TYyg"/>
             * </properties>
             *
             * SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, props);
             */
            XNode properties = xNode.evalNode("properties");
            propertiesElement(properties);

            // 设置
            Properties settings = settingsAsProperties(xNode.evalNode("settings"));
            // 加载虚拟文件系统实现类
            loadCustomVfs(settings);
            // 加载日志系统实现类
            loadCustomLogImpl(settings);
        } catch (Exception e) {
            throw new RuntimeException("设置全局配置文件Configuration时出错，原因： " + e, e);
        }

    }

    /**
     * 加载日志系统实现类
     *
     * @param settings
     */
    private void loadCustomLogImpl(Properties settings) {
        // 调用父类方法，通过别名返回实体类
        Class<? extends Log> logImpl = resplveClass(settings.getProperty("logImpl"));
        // 设置日志系统
        configuration.setLogImpl(logImpl);
    }

    /**
     * 加载虚拟文件系统实现类
     *
     * @param settings
     * @throws ClassNotFoundException
     */
    private void loadCustomVfs(Properties settings) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        // 获取设置里面自定义的虚拟文件系统实现
        String value = settings.getProperty("vfsImpl");
        if (value != null) {
            // 多个配置之间以逗号隔开
            String[] clazzes = value.split(",");
            for (String clazz : clazzes) {
                if (!clazz.isEmpty()) {
                    // 通过全类名加载虚拟文件系统的实现类，该类继承VFS父类
                    Class<? extends VFS> vfsImpl = (Class<? extends VFS>) Resources.classForName(clazz);
                    // 测试是否读取成功
                    vfsImpl.newInstance().toString();
                    // 设置全句配置
                    configuration.setVfsImpl(vfsImpl);
                }
            }
        }
    }

    /**
     * 设置校验
     *
     * @param settings
     * @return
     */
    private Properties settingsAsProperties(XNode settings) {
        if (settings == null) {
            return new Properties();
        }
        Properties props = settings.getChildrenAsProperties();

        // XMLConfigBuilder在初始化的时候已经初始化了反射工厂，此时反射工厂中缓存为空，
        // 在调用MetaClass.forClass时，会将入参Configuration的反射实体类创建并存放到反射工厂中
        MetaClass metaClass = MetaClass.forClass(Configuration.class, localReflectorFactory);
        // 校验属性设置是否合法，是否符合Configuration类的属性名称
        for (Object key : props.keySet()) {
            if (!metaClass.hasSetter(String.valueOf(key))) {
                throw new RuntimeException("设置的" + key + "不合法。确保你正确拼写(区分大小写)。");
            }
        }

        return props;

    }

    /**
     * 解析属性集配置
     *
     * @param properties
     */
    private void propertiesElement(XNode properties) throws Exception {
        if (properties != null) {
            // 处理在 properties 元素的子元素中设置
            /**
             * <properties resource="org/mybatis/example/config.properties">
             *   <property name="username" value="dev_user"/>
             *   <property name="password" value="F2Fa3!33TYyg"/>
             * </properties>
             */
            Properties def = properties.getChildrenAsProperties();

            String resource = properties.getStringAttribute("resource");
            String url = properties.getStringAttribute("url");
            if (resource != null && url != null) {
                throw new RuntimeException("属性集配置不允许url和resource同时配置");
            }
            // 读取资源文件中的配置
            if (resource != null) {
                def.putAll(Resources.getResourceAsProperties(resource));
            } else if (url != null) {
                def.putAll(Resources.getResourceAsProperties(url));
            }

            // 将构造器时传入的properties在重新赋值一遍给def，否则会被覆盖
            Properties vars = configuration.getVariables();
            if (vars != null) {
                def.putAll(vars);
            }

            // 设置更新XPath属性
            xPathParser.setVariables(def);

            // 构建全剧配置文件configuration的variables属性
            configuration.setVariables(def);
        }
    }
}
