package com.jingxc.ibatis.session;

import com.jingxc.ibatis.io.VFS;
import com.jingxc.ibatis.logging.Log;
import com.jingxc.ibatis.logging.LogFactory;
import com.jingxc.ibatis.logging.slf4j.Slf4jImpl;
import com.jingxc.ibatis.type.TypeAliasRegistry;

import java.util.Properties;

public class Configuration {

    // 一个持久的属性集
    protected Properties variables = new Properties();

    // 全局性的缓存开关,全局性地开启或关闭所有映射器配置文件中已配置的任何缓存。
    protected boolean cacheEnabled = true;

    // 虚拟文件系统,自定义 VFS 的实现的类全限定名，以逗号分隔。
    protected Class<? extends VFS> vfsImpl;

    // 日志系统实现,指定 MyBatis 所用日志的具体实现，未指定时将自动查找。
    protected Class<? extends Log> logImpl;

    // 类型别名注册类，实现类型简称名称到实体类一一对应，例：string<==>String.class
    protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();

    public Configuration() {
        // 注册日志类别名
        typeAliasRegistry.resolveAlias("SLF4J", Slf4jImpl.class);
        // typeAliasRegistry.registerAlias("COMMONS_LOGGING", JakartaCommonsLoggingImpl.class);
        // typeAliasRegistry.registerAlias("LOG4J", Log4jImpl.class);
        // typeAliasRegistry.registerAlias("LOG4J2", Log4j2Impl.class);
        // typeAliasRegistry.registerAlias("JDK_LOGGING", Jdk14LoggingImpl.class);
        // typeAliasRegistry.registerAlias("STDOUT_LOGGING", StdOutImpl.class);
        // typeAliasRegistry.registerAlias("NO_LOGGING", NoLoggingImpl.class);

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
            // 设置日志系统，初始化并使用
            LogFactory.useCustomLogging(this.logImpl);
        }
    }
}
