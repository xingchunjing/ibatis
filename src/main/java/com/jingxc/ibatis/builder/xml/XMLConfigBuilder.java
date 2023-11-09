package com.jingxc.ibatis.builder.xml;

import com.jingxc.ibatis.parsing.XPathParser;

import java.io.InputStream;
import java.util.Properties;

public class XMLConfigBuilder {
    public XMLConfigBuilder(InputStream inputStream, String environment, Properties props) {
        // XPathParser基于Java XPath解析器，用于解析Mybatis配置文件
        this(new XPathParser(inputStream, true, props, new XMLMapperEntityResolver()), environment, props);
    }

    private XMLConfigBuilder(XPathParser parser, String environment, Properties props) {
        // 创建Configuration对象，并通过TypeAliasRegistry注册一些Mybatis内部相关类的别名
    }
}
