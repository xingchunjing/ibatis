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
        
    }
}
