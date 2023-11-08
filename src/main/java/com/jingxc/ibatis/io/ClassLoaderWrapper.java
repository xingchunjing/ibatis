package com.jingxc.ibatis.io;

import java.io.InputStream;

/**
 * 类加载器包装类：用于包装对多个类加载器的访问，使它们作为一个类工作
 */
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
