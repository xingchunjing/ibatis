package com.jingxc.ibatis.session;

import com.jingxc.ibatis.builder.xml.XMLConfigBuilder;

import java.io.InputStream;
import java.util.Properties;

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

    /**
     * 需要传入运行环境的信息值，对应配置文件中environment标签值，配置文件中可以配置多个environment
     *
     * @param inputStream
     * @param environment
     * @return
     */
    public SqlSessionFactory build(InputStream inputStream, String environment) {
        return build(inputStream, environment, null);
    }

    public SqlSessionFactory build(InputStream inputStream, Properties properties) {
        return build(inputStream, null, properties);
    }

    public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
        // XMLConfigBuilder：用来解析xml配置文件
        // 使用构建者模式，降低耦合，分离复杂对象的创建
        // 1.创建XPathParser解析器对象，根据inputStream解析成Document对象; 2.创建全剧配置对象Configuration
        XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder(inputStream, environment, properties);

        // XMLConfigBuilder构建者的parse方法，返回得失全局配置类
        Configuration configuration = xmlConfigBuilder.parse();

        return null;
    }
}
