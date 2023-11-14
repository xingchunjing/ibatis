package com.jingxc.ibatis.builder;

import com.jingxc.ibatis.session.Configuration;
import com.jingxc.ibatis.type.TypeAliasRegistry;

public abstract class BaseBuilder {

    protected final Configuration configuration;

    protected final TypeAliasRegistry typeAliasRegistry;

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
    }

    /**
     * // 通过别名放回对应的实体类
     *
     * @param alias
     * @param <T>
     * @return
     */
    protected <T> Class<? extends T> resplveClass(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return resolveAlias(alias);
        } catch (Exception e) {
            throw new RuntimeException("解析类出错。原因： " + e, e);
        }
    }

    protected <T> Class<? extends T> resolveAlias(String alias) {
        // 通过别名放回对应的实体类
        return typeAliasRegistry.resolveAlias(alias);
    }

}
