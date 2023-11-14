package com.jingxc.ibatis.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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

    /**
     * 读取资源文件返回Properties
     */
    public static Properties getResourceAsProperties(String resource) throws IOException {
        Properties properties = new Properties();
        try (InputStream in = getResourceAsStream(resource)) {
            properties.load(in);
        }
        return properties;
    }

    /**
     * 通过全类名加载类
     *
     * @param clazz
     * @return
     */
    public static Class<?> classForName(String clazz) throws ClassNotFoundException {
        return classLoaderWrapper.classForName(clazz);
    }
}
