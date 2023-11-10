package com.jingxc.ibatis.builder.xml;

import com.jingxc.ibatis.builder.BaseBuilder;
import com.jingxc.ibatis.parsing.XNode;
import com.jingxc.ibatis.parsing.XPathParser;
import com.jingxc.ibatis.session.Configuration;

import java.io.InputStream;
import java.util.Properties;

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
        XNode xNode1 = xNode.evalNode("/properties");
        return null;
    }
}
