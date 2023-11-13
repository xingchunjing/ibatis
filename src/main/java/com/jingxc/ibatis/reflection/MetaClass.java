package com.jingxc.ibatis.reflection;

public class MetaClass {

    private final ReflectionFactory reflectionFactory;
    private final Reflector reflector;

    public MetaClass(Class<?> type, ReflectionFactory reflectorFactory) {
        this.reflectionFactory = reflectorFactory;
        this.reflector = reflectorFactory.findForClass(type);
    }

    /**
     * 通过class和反射工厂初始化反射实体类
     *
     * @param type
     * @param reflectorFactory
     * @return
     */
    public static MetaClass forClass(Class<?> type, ReflectionFactory reflectorFactory) {
        return new MetaClass(type, reflectorFactory);
    }
}
