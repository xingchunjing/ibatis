package com.jingxc.ibatis.builder;

import com.jingxc.ibatis.session.Configuration;

public abstract class BaseBuilder {

    protected final Configuration configuration;

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
    }
}
