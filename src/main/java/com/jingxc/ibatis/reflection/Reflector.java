package com.jingxc.ibatis.reflection;

import com.jingxc.ibatis.reflection.property.PropertyNamer;
import com.jingxc.ibatis.util.MapUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class Reflector {

    private final Class<?> type;
    private Constructor<?> defaultConstructor;

    public Reflector(Class<?> clazz) {

        type = clazz;
        // 无参构造器
        addDefaultConstructor(clazz);
        // get方法
        addGetMethods(clazz);
        // set方法
        addSetMethods(clazz);
        //

    }

    /**
     * 设置set方法
     *
     * @param clazz
     */
    private void addSetMethods(Class<?> clazz) {
        Map<String, List<Method>> conflictingSetters = new HashMap<>();
        Method[] methods = getClassMethods(clazz);
        Arrays.stream(methods).filter(m -> m.getParameterTypes().length == 1 && PropertyNamer.isSetter(m.getName()))
                .forEach(m -> addMethodConflict(conflictingSetters, PropertyNamer.methodToProperty(m.getName()), m));

    }

    /**
     * 设置get方法
     *
     * @param clazz
     */
    private void addGetMethods(Class<?> clazz) {
        Map<String, List<Method>> conflictingGetters = new HashMap<>();
        Method[] methods = getClassMethods(clazz);
        Arrays.stream(methods).filter(m -> m.getParameterTypes().length == 0 && PropertyNamer.isGtter(m.getName()))
                .forEach(m -> addMethodConflict(conflictingGetters, PropertyNamer.methodToProperty(m.getName()), m));

        resolveGetterConflicts(conflictingGetters);
    }

    private void resolveGetterConflicts(Map<String, List<Method>> conflictingGetters) {
    }

    private void addMethodConflict(Map<String, List<Method>> conflictingGetters, String name, Method m) {
        if (isValidPropertyName(name)) {
            List<Method> methods = MapUtil.computeIfAbsent(conflictingGetters, name, k -> new ArrayList<>());
            methods.add(m);
        }
    }

    /**
     * 排除$,serialVersionUID,class属性方法
     *
     * @param name
     * @return
     */
    private boolean isValidPropertyName(String name) {
        return !(name.startsWith("$") || "serialVersionUID".equals(name) || "class".equals(name));
    }

    /**
     * 获取class的所有方法
     *
     * @param clazz
     * @return
     */
    private Method[] getClassMethods(Class<?> clazz) {
        Map<String, Method> uniqueMethods = new HashMap<>();
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {

            // 自己内部方法
            addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods());

            // 所实现的接口的方法
            Class<?>[] interfaces = currentClass.getInterfaces();
            for (Class<?> anInterface : interfaces) {
                addUniqueMethods(uniqueMethods, anInterface.getDeclaredMethods());
            }
            // 通过自循环获取父类的方法
            currentClass = currentClass.getSuperclass();
        }

        Collection<Method> methods = uniqueMethods.values();
        return methods.toArray(new Method[0]);

    }

    /**
     * 存储方法模版
     *
     * @param uniqueMethods
     * @param methods
     */
    private void addUniqueMethods(Map<String, Method> uniqueMethods, Method[] methods) {
        for (Method currentMethod : methods) {
            if (!currentMethod.isBridge()) {
                String signature = getSignature(currentMethod);
                // 如果不存在则存储
                if (!uniqueMethods.containsKey(signature)) {
                    uniqueMethods.put(signature, currentMethod);
                }
            }
        }
    }

    /**
     * 转换获取方法格式：String#myMethod:String,Integer
     *
     * @param method
     * @return
     */
    private String getSignature(Method method) {
        StringBuilder sb = new StringBuilder();
        Class<?> returnType = method.getReturnType();
        if (returnType != null) {
            sb.append(returnType.getName()).append("#");
        }
        sb.append(method.getName());
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            sb.append(i == 0 ? ':' : ',').append(parameterTypes[i].getName());
        }
        return sb.toString();
    }

    /**
     * 设置无参构造器
     *
     * @param clazz
     */
    private void addDefaultConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        // 获取无参构造器
        Arrays.stream(constructors).filter(constructor -> constructor.getParameterTypes().length == 0).findAny()
                .ifPresent(constructor -> this.defaultConstructor = constructor);
    }
}
