package com.jingxc.ibatis.session;

import java.util.Properties;

public class Configuration {

    // 一个持久的属性集
    protected Properties variables = new Properties();

    public Properties getVariables() {
        return variables;
    }

    public void setVariables(Properties variables) {
        this.variables = variables;
    }
}
