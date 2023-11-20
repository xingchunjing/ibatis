package com.jingxc.ibatis.type;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Alias {

    /**
     * 标记别名
     *
     * @return
     */
    String value();
}
