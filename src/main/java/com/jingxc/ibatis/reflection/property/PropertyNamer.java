package com.jingxc.ibatis.reflection.property;

import java.util.Locale;

public class PropertyNamer {

    /**
     * 获取get/is方法
     *
     * @param name
     * @return
     */
    public static boolean isGtter(String name) {
        return (name.startsWith("get") && name.length() > 3) || (name.startsWith("is") && name.length() > 2);
    }

    /**
     * 获取setter方法
     *
     * @param name
     * @return
     */
    public static boolean isSetter(String name) {
        return name.startsWith("set") && name.length() > 3;
    }

    public static String methodToProperty(String name) {
        if (name.startsWith("is")) {
            name = name.substring(2);
        } else if (name.startsWith("get") || name.startsWith("set")) {
            name = name.substring(3);
        } else {
            throw new RuntimeException("Error parsing property name '" + name + "'.  Didn't start with 'is', 'get' or 'set'.");
        }

        if (name.length() == 1 || (name.length() > 1 && !Character.isUpperCase(name.charAt(1)))) {
            name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
        }
        return name;
    }

}
