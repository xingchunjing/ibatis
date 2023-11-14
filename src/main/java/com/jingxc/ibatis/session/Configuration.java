package com.jingxc.ibatis.session;

import com.jingxc.ibatis.io.VFS;
import com.jingxc.ibatis.logging.Log;
import com.jingxc.ibatis.logging.LogFactory;
import com.jingxc.ibatis.type.TypeAliasRegistry;

import java.util.Properties;

public class Configuration {

    // 一个持久的属性集
    protected Properties variables = new Properties();

    // 全局性的缓存开关
    protected boolean cacheEnabled = true;

    // 虚拟文件系统
    protected Class<? extends VFS> vfsImpl;

    // 日志系统实现
    protected Class<? extends Log> logImpl;

    // 类型别名注册类，实现类型简称名称到实体类一一对应，例：string<==>String.class
    protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();

    public Configuration() {
        // TODO 初始化属性集
    }

    public Properties getVariables() {
        return variables;
    }

    public void setVariables(Properties variables) {
        this.variables = variables;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    /**
     * 设置虚拟文件系统实现类
     *
     * @param vfsImpl
     */
    public void setVfsImpl(Class<? extends VFS> vfsImpl) {
        if (vfsImpl != null) {
            this.vfsImpl = vfsImpl;
            VFS.addImplClass(this.vfsImpl);
        }
    }

    /**
     * 获取类型别名配置类
     *
     * @return
     */
    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }

    /**
     * 设置日志系统
     *
     * @param logImpl
     */
    public void setLogImpl(Class<? extends Log> logImpl) {
        if (logImpl != null) {
            this.logImpl = logImpl;
            // 设置日志系统
            LogFactory.useCustomLogging(this.logImpl);
        }
    }
}
