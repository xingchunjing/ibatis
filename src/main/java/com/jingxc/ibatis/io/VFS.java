package com.jingxc.ibatis.io;

import com.jingxc.ibatis.logging.Log;
import com.jingxc.ibatis.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

/**
 * 虚拟文件系统类
 * 通过内部类完成虚拟文件路径对应关系的创建，单例
 * 主要实现部分在静态内部类VFSHolder
 */
public abstract class VFS {

    private static final Log log = LogFactory.getLog(VFS.class);

    // 存储配置文件配置的虚拟文件系统实现类
    public static final List<Class<? extends VFS>> USER_IMPLEMENTATIONS = new ArrayList<>();

    // 默认虚拟文件映射配置类
    public static final Class<?>[] IMPLEMENTATIONS = {JBoss6VFS.class};

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

    public static VFS getInstance() {
        return VFSHolder.INSTANCE;
    }

    private static class VFSHolder {
        static final VFS INSTANCE = createVFS();

        static VFS createVFS() {
            List<Class<? extends VFS>> impls = new ArrayList<>();
            // 将设置中配置的VFS进行初始化
            impls.addAll(USER_IMPLEMENTATIONS);
            // 默认设置初始化
            impls.addAll(Arrays.asList((Class<? extends VFS>[]) IMPLEMENTATIONS));

            VFS vfs = null;
            for (int i = 0; vfs == null || !vfs.isValid(); i++) {
                Class<? extends VFS> impl = impls.get(i);
                try {
                    vfs = impl.getDeclaredConstructor().newInstance();
                    if (!vfs.isValid() && log.isDebugEnabled()) {
                        log.debug("VFS 的实现 " + impl.getName() + " 在当前环境下是不可使用的.");
                    }
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                         NoSuchMethodException e) {
                    log.error("初始化失败 " + impl, e);
                    return null;
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("使用 VFS 适配器 " + vfs.getClass().getName());
            }

            return vfs;
        }
    }

    public List<String> list(String path) throws IOException {
        List<String> names = new ArrayList<>();
        // 获取文件的实际物理目录地址
        List<URL> resources = getResources(path);
        for (URL url : resources) {
            // list()将文件实际物理地址转化为spring的java类路径地址，并获取包下类
            // file:/Users/jingxc/service/sts-project/ibatis/target/classes/com/jingxc/ibatis/type/test
            // com/jingxc/ibatis/type/test/Users.class
            names.addAll(list(url, path));
        }
        return names;
    }

    /**
     * 读取目录文件规则
     *
     * @param url
     * @param forPath
     * @return
     * @throws IOException
     */
    protected abstract List<String> list(URL url, String forPath) throws IOException;

    /**
     * 是否可用
     *
     * @return
     */
    public abstract boolean isValid();

    /**
     * 获取路径地址
     *
     * @param path
     * @return
     * @throws IOException
     */
    protected static List<URL> getResources(String path) throws IOException {
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
        return Collections.list(resources);
    }

}
