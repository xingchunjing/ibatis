package com.jingxc.ibatis.logging.slf4j;

import com.jingxc.ibatis.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.spi.LocationAwareLogger;

public class Slf4jImpl implements Log {

    private Log log;

    /**
     * 构造器
     *
     * @param clazz
     */
    public Slf4jImpl(String clazz) {
        // 获取org.slf4j.Logger
        Logger logger = LoggerFactory.getLogger(clazz);
        if (logger instanceof LocationAwareLogger) {
            try {
                logger.getClass().getMethod("log", Marker.class, String.class, int.class, String.class,
                        Object[].class, Throwable.class);
                log = new Slf4jLocationAwareLoggerImpl((LocationAwareLogger) logger);
                return;
            } catch (NoSuchMethodException e) {

            }
        }
        // Logger is not LocationAwareLogger or slf4j version < 1.6
        log = new Slf4jLoggerImpl(logger);
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public void debug(String s) {
        log.debug(s);
    }
}
