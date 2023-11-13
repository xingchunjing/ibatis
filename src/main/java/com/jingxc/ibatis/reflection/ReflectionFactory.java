package com.jingxc.ibatis.reflection;

public interface ReflectionFactory {

    boolean isClassCacheEnabled();

    void setClassCacheEnabled(boolean classCacheEnabled);

    Reflector findForClass(Class<?> type);
}
