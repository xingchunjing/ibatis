package com.jingxc.ibatis.reflection;

import com.jingxc.ibatis.util.MapUtil;

import java.util.concurrent.ConcurrentHashMap;

public class DefaultReflectionFactory implements ReflectionFactory {

    // 是否缓存反射实体类Reflector
    private boolean classCacheEnabled = true;

    // 缓存
    private final ConcurrentHashMap<Class<?>, Reflector> reflectorMap = new ConcurrentHashMap<>();

    public DefaultReflectionFactory() {
    }

    @Override
    public boolean isClassCacheEnabled() {
        return classCacheEnabled;
    }

    @Override
    public void setClassCacheEnabled(boolean classCacheEnabled) {
        this.classCacheEnabled = classCacheEnabled;
    }

    @Override
    public Reflector findForClass(Class<?> type) {
        // 如果启用缓存，则从缓存中获取反射实体类，默认开启，可通过setClassCacheEnabled设置
        if (classCacheEnabled) {
            // 如果map中存在则从map中获取，如果不存在则创建并存储到map中
            return MapUtil.computeIfAbsent(reflectorMap, type, Reflector::new);
        } else {
            // 新建反射实体类
            return new Reflector(type);
        }
    }
}
