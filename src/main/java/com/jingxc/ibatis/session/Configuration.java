package com.jingxc.ibatis.session;

import java.util.Properties;

public class Configuration {

    // 一个持久的属性集
    protected Properties variables = new Properties();
    protected boolean cacheEnabled = true;

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
}
