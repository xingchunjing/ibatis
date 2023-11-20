package com.jingxc.ibatis.type;

import com.jingxc.ibatis.io.ResolverUtil;
import com.jingxc.ibatis.io.Resources;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.util.*;

public class TypeAliasRegistry {

    // 别名类型map集
    private final Map<String, Class<?>> typeAliases = new HashMap<>();

    public TypeAliasRegistry() {
        // 构造器直接初始化别名类型集合
        registerAlias("string", String.class);

        registerAlias("byte", Byte.class);
        registerAlias("long", Long.class);
        registerAlias("short", Short.class);
        registerAlias("int", Integer.class);
        registerAlias("integer", Integer.class);
        registerAlias("double", Double.class);
        registerAlias("float", Float.class);
        registerAlias("boolean", Boolean.class);

        registerAlias("byte[]", Byte[].class);
        registerAlias("long[]", Long[].class);
        registerAlias("short[]", Short[].class);
        registerAlias("int[]", Integer[].class);
        registerAlias("integer[]", Integer[].class);
        registerAlias("double[]", Double[].class);
        registerAlias("float[]", Float[].class);
        registerAlias("boolean[]", Boolean[].class);

        registerAlias("_byte", byte.class);
        registerAlias("_long", long.class);
        registerAlias("_short", short.class);
        registerAlias("_int", int.class);
        registerAlias("_integer", int.class);
        registerAlias("_double", double.class);
        registerAlias("_float", float.class);
        registerAlias("_boolean", boolean.class);

        registerAlias("_byte[]", byte[].class);
        registerAlias("_long[]", long[].class);
        registerAlias("_short[]", short[].class);
        registerAlias("_int[]", int[].class);
        registerAlias("_integer[]", int[].class);
        registerAlias("_double[]", double[].class);
        registerAlias("_float[]", float[].class);
        registerAlias("_boolean[]", boolean[].class);

        registerAlias("date", Date.class);
        registerAlias("decimal", BigDecimal.class);
        registerAlias("bigdecimal", BigDecimal.class);
        registerAlias("biginteger", BigInteger.class);
        registerAlias("object", Object.class);

        registerAlias("date[]", Date[].class);
        registerAlias("decimal[]", BigDecimal[].class);
        registerAlias("bigdecimal[]", BigDecimal[].class);
        registerAlias("biginteger[]", BigInteger[].class);
        registerAlias("object[]", Object[].class);

        registerAlias("map", Map.class);
        registerAlias("hashmap", HashMap.class);
        registerAlias("list", List.class);
        registerAlias("arraylist", ArrayList.class);
        registerAlias("collection", Collection.class);
        registerAlias("iterator", Iterator.class);

        registerAlias("ResultSet", ResultSet.class);
    }

    public <T> Class<? extends T> resolveAlias(String alias) {
        try {
            // 如果别名为null，则返回null
            if (alias == null) {
                return null;
            }
            // 转为小写字符
            String key = alias.toLowerCase(Locale.ENGLISH);
            Class<T> value;
            // 如果别名中包含该对应则直接返回该类型
            if (typeAliases.containsKey(key)) {
                value = (Class<T>) typeAliases.get(key);
            } else {
                // 如果不包含直接使用类加载器加载实体类
                value = (Class<T>) Resources.classForName(alias);
            }
            return value;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not resolve type alias '" + alias + "'.  Cause: " + e, e);
        }
    }

    public void resolveAlias(String alias, Class<?> clazz) {
        if (alias == null) {
            throw new RuntimeException("别名参数不能为空");
        }
        String key = alias.toLowerCase(Locale.ENGLISH);
        if (typeAliases.containsKey(key) && typeAliases.get(key) != null && !typeAliases.get(key).equals(clazz)) {
            throw new RuntimeException("The alias '" + alias + "' is already mapped to the value '" + typeAliases.get(key).getName() + "'.");
        }
        typeAliases.put(key, clazz);
    }

    /**
     * 通过class配置别名
     *
     * @param alias
     * @param value
     */
    public void registerAlias(String alias, Class<?> value) {
        if (alias == null) {
            throw new RuntimeException("The parameter alias cannot be null");
        }
        String key = alias.toLowerCase(Locale.ENGLISH);
        if (typeAliases.containsKey(key) && typeAliases.get(key) != null && typeAliases.get(key).equals(value)) {
            throw new RuntimeException("The alias '" + alias + "' is already mapped to the value '" + typeAliases.get(key).getName() + "'.");
        }
        typeAliases.put(key, value);
    }

    /**
     * 通过包名配置别名
     *
     * @param typeAliasPackage
     */
    public void registerAliases(String typeAliasPackage) {
        registerAliases(typeAliasPackage, Object.class);
    }

    public void registerAliases(String packageName, Class<?> superType) {
        ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
        ResolverUtil.IsA isA = new ResolverUtil.IsA(superType);
        resolverUtil.find(isA, packageName);
        Set<Class<? extends Class<?>>> classes = resolverUtil.getClasses();
        for (Class<?> type : classes) {
            if (!type.isAnonymousClass() && !type.isInterface() && !type.isMemberClass()) {
                registerAliases(type);
            }
        }
    }

    public void registerAliases(Class<?> type) {
        // 默认别名
        String alias = type.getSimpleName();
        // 获取实体类上别名
        Alias typeAnnotation = type.getAnnotation(Alias.class);
        if (typeAnnotation != null) {
            alias = typeAnnotation.value();
        }
        registerAlias(alias, type);
    }
}
