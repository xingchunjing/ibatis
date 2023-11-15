package com.jingxc.ibatis.logging;

public interface Log {

    boolean isDebugEnabled();

    void debug(String s);

    void error(String s);

    void error(String s, Throwable e);
}
