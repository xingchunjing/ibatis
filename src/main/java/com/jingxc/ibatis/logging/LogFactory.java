package com.jingxc.ibatis.logging;

import java.lang.reflect.Constructor;

public class LogFactory {

    public static final String MARKER = "MYBATIS";

    private static Constructor<? extends Log> logConstructor;

    /**
     * 设置客户端配置的日志系统
     *
     * @param clazz
     */
    public static void useCustomLogging(Class<? extends Log> clazz) {
        setImplementation(clazz);
    }

    /**
     * 通过构造器构造日志系统并使用
     *
     * @param clazz
     */
    private static void setImplementation(Class<? extends Log> clazz) {
        try {
            // 通过反射获取构造器
            Constructor<? extends Log> constructor = clazz.getConstructor(String.class);
            Log log = constructor.newInstance(LogFactory.class.getName());
            if (log.isDebugEnabled()) {
                log.debug("日志系统初始化成功，使用 '" + clazz);
            }
            logConstructor = constructor;
        } catch (Throwable e) {
            throw new RuntimeException("设置日志系统出错.  原因: " + e, e);
        }
    }

    public static Log getLog(Class<?> clazz) {
        return getLog(clazz.getName());
    }

    public static Log getLog(String logger) {
        try {
            return logConstructor.newInstance(logger);
        } catch (Throwable t) {
            throw new RuntimeException("创建logger出错 " + logger + ".  原因: " + t, t);
        }
    }
}
