package com.jingxc.ibatis.io;

import java.util.ArrayList;
import java.util.List;

/**
 * 虚拟文件系统类
 */
public abstract class VFS {

    /**
     * 存储配置文件配置的虚拟文件系统实现类
     */
    public static final List<Class<? extends VFS>> USER_IMPLEMENTATIONS = new ArrayList<>();

    /**
     * 添加实现
     *
     * @param vfsImpl
     */
    public static void addImplClass(Class<? extends VFS> vfsImpl) {
        if (vfsImpl != null) {
            USER_IMPLEMENTATIONS.add(vfsImpl);
        }
    }
}
