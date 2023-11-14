package com.jingxc.ibatis.reflection;

import com.jingxc.ibatis.reflection.property.PropertyTokenizer;

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

    public boolean hasSetter(String name) {
        // 名称属性迭代器
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            // 判断是否含有该属性的set方法
            if (reflector.hasSetter(prop.getName())) {
                MetaClass metaProp = metaClassForProperty(prop.getName());
                return metaProp.hasSetter(prop.getChildren());
            } else {
                return false;
            }
        } else {
            return reflector.hasSetter(prop.getName());
        }
    }

    private MetaClass metaClassForProperty(String name) {
        Class<?> propType = reflector.getGetterType(name);
        return MetaClass.forClass(propType, reflectionFactory);
    }
}
