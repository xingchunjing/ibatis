package com.jingxc.ibatis.io;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class JBoss6VFS extends VFS {

    @Override
    protected List<String> list(URL url, String forPath) throws IOException {
        // 在自己实现的类中参考了该方法，这里就不做过多的书写了
        return null;
    }

    @Override
    public boolean isValid() {
        // 设置为不可用
        return false;
    }
}
