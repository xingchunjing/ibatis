package com.jingxc.ibatis.io;

import com.jingxc.ibatis.logging.Log;
import com.jingxc.ibatis.logging.LogFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResolverUtil<T> {

    private static final Log log = LogFactory.getLog(ResolverUtil.class);

    private ClassLoader classloader;

    // 匹配获取实体类型
    private Set<Class<? extends T>> matches = new HashSet<>();

    public ResolverUtil<T> find(Test test, String packageName) {

        // 转换java类路径为文件路径
        String path = getPackagePath(packageName);
        try {
            List<String> children = VFS.getInstance().list(path);
            for (String child : children) {
                addIfMatching(test, child);
            }
        } catch (IOException e) {
            log.error("读取包目录出错: " + packageName, e);
        }
        return this;
    }

    private void addIfMatching(Test test, String fqn) {
        try {
            // 将.class去掉，获取实际的类路径
            String externalName = fqn.substring(0, fqn.indexOf('.')).replace('/', '.');
            // 获取类加载器
            ClassLoader classLoader = getClassLoader();
            if (log.isDebugEnabled()) {
                log.debug("检查 " + externalName + " 是否匹配 [" + test + "]");
            }

            Class<?> type = classLoader.loadClass(externalName);
            if (test.matchs(type)) {
                matches.add((Class<T>) type);
            }
        } catch (Throwable t) {
            log.error("匹配检查失败 '" + fqn + "'" + " 由于 "
                    + t.getClass().getName() + " 原因: " + t.getMessage());
        }
    }

    /**
     * 获取返回实体类型集
     *
     * @return
     */
    public Set<Class<? extends T>> getClasses() {
        return matches;
    }

    public ClassLoader getClassLoader() {
        return classloader == null ? Thread.currentThread().getContextClassLoader() : classloader;
    }

    /**
     * 转换java类路径为文件路径
     *
     * @param packageName
     * @return
     */
    private String getPackagePath(String packageName) {
        return packageName == null ? null : packageName.replace('.', '/');
    }

    public interface Test {
        boolean matchs(Class<?> type);
    }

    public static class IsA implements Test {

        private Class<?> parent;

        public IsA(Class<?> parent) {
            this.parent = parent;
        }

        @Override
        public boolean matchs(Class<?> type) {
            return type != null && parent.isAssignableFrom(type);
        }
    }

}
