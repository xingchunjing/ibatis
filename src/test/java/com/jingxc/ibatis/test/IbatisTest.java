package com.jingxc.ibatis.test;

import com.jingxc.ibatis.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class IbatisTest {

    @Test
    public void test1() throws IOException {

        // 1.通过类加载器对配置文件进行加载，加载成字节数入流，存到内存中，注意：配置文件并没有被解析
        InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");

        System.out.println(resourceAsStream);

    }
}
