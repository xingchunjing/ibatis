package com.jingxc.ibatis.builder.xml;

import com.jingxc.ibatis.io.Resources;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * 如果ＳＡＸ应用程序实现自定义处理外部实体,则必须实现此接口EntityResolver,
 * 并使用setEntityResolver方法向SAX 驱动器注册一个实例
 */
public class XMLMapperEntityResolver implements EntityResolver {

    private static final String IBATIS_CONFIG_SYSTEM = "ibatis-3-config.dtd";
    private static final String MYBATIS_CONFIG_SYSTEM = "mybatis-3-config.dtd";

    private static final String MYBATIS_MAPPER_SYSTEM = "mybatis-3-mapper.dtd";

    private static final String IBATIS_MAPPER_SYSTEM = "ibatis-3-mapper.dtd";

    private static final String MYBATIS_CONFIG_DTD = "com/jingxc/ibatis/builder/xml/mybatis-3-config.dtd";
    private static final String MYBATIS_MAPPER_DTD = "com/jingxc/ibatis/builder/xml/mybatis-3-mapper.dtd";

    /**
     * 对于解析一个xml,sax
     * 首先会读取该xml文档上的声明,根据声明去寻找相应的dtd定义,以便对文档的进行验证,
     * 默认的寻找规则,(即:通过网络,实现上就是声明DTD的地址URI地址来下载DTD声明),
     * 并进行认证,下载的过程是一个漫长的过程,而且当网络不可用时,这里会报错,就是因为相应的dtd没找到
     *
     * @param publicId The public identifier of the external entity
     *                 being referenced, or null if none was supplied.
     * @param systemId The system identifier of the external entity
     *                 being referenced.
     * @return
     * @throws SAXException
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
        // EntityResolver 的作用就是项目本身就可以提供一个如何寻找DTD 的声明方法,
        // 即:由程序来实现寻找DTD声明的过程,比如我们将DTD放在项目的某处在实现时直接将此文档读取并返回个SAX即可,这样就避免了通过网络来寻找DTD的声明
        try {
            if (systemId != null) {
                String lowerCaseSystemId = systemId.toLowerCase(Locale.ENGLISH);
                if (lowerCaseSystemId.contains(MYBATIS_CONFIG_SYSTEM) || lowerCaseSystemId.contains(IBATIS_CONFIG_SYSTEM)) {
                    return getInputSource(MYBATIS_CONFIG_DTD, publicId, systemId);
                } else if (lowerCaseSystemId.contains(MYBATIS_MAPPER_SYSTEM) || lowerCaseSystemId.contains(IBATIS_MAPPER_SYSTEM)) {
                    return getInputSource(MYBATIS_MAPPER_DTD, publicId, systemId);
                }
            }
            return null;
        } catch (Exception e) {
            throw new SAXException(e.toString());
        }
    }

    /**
     * 具体的声明方法
     *
     * @param path
     * @param publicId
     * @param systemId
     * @return
     */
    private InputSource getInputSource(String path, String publicId, String systemId) {
        InputSource source = null;
        if (path != null) {
            try {
                InputStream in = Resources.getResourceAsStream(path);
                source = new InputSource(in);
                source.setSystemId(systemId);
                source.setPublicId(publicId);
            } catch (IOException ignore) {
                ignore.printStackTrace();
            }
        }
        return source;
    }
}
