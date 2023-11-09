package com.jingxc.ibatis.test;

import com.jingxc.ibatis.io.Resources;
import com.jingxc.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class IbatisTest {

    @Test
    public void test1() throws IOException {

        // 1.通过类加载器对配置文件进行加载，加载成字节数入流，存到内存中，注意：配置文件并没有被解析
        InputStream inputStream = Resources.getResourceAsStream("sqlMapConfig.xml");

        // 通过构建者模式，构建SqlSessionFactory工厂
        new SqlSessionFactoryBuilder().build(inputStream)
        System.out.println(inputStream);

    }
}
