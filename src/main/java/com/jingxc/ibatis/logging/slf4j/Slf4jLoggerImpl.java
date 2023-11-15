package com.jingxc.ibatis.logging.slf4j;

import com.jingxc.ibatis.logging.Log;
import org.slf4j.Logger;

public class Slf4jLoggerImpl implements Log {

    private final Logger log;

    public Slf4jLoggerImpl(Logger logger) {
        this.log = logger;
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public void debug(String s) {
        log.debug(s);
    }

    @Override
    public void error(String s) {
        log.error(s);
    }

    @Override
    public void error(String s, Throwable e) {
        log.error(s, e);
    }
}
