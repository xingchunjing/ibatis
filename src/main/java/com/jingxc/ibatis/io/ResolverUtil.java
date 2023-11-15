package com.jingxc.ibatis.io;

import com.jingxc.ibatis.logging.Log;
import com.jingxc.ibatis.logging.LogFactory;

import java.io.IOException;
import java.util.List;

public class ResolverUtil<T> {

    private static final Log log = LogFactory.getLog(ResolverUtil.class);

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

    private void addIfMatching(Test test, String child) {
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
