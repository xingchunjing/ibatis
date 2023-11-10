package com.jingxc.ibatis.parsing;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.util.Properties;

public class XPathParser {

    private boolean validation;
    // 使 XML 加载的过程中不需要通过网络下载约束文件。这种情况下，通过EntityResolver告诉解析器如何找到正确的约束文件
    private EntityResolver entityResolver;

    // 属性集
    private Properties variables;

    // XPath是一种在XML文档中定位节点的语言，它可以根据节点的属性、元素名称等条件来进行查询。在Java中，可以使用XPath来操作XML文档，实现对特定节点的查找、遍历和修改等操作
    private XPath xpath;

    // 用于描述HTML或XML文档。它提供了许多方法，可以获取文档的信息，其中包括文档的标题、元素、属性等等。使用Document类，我们可以方便地获取HTML或XML文档中的信息，以便我们对文档进行各种操作。
    private final Document document;

    public XPathParser(InputStream inputStream, boolean validation, Properties variables, EntityResolver entityResolver) {
        // 通用构造
        commonConstructor(validation, variables, entityResolver);
        // 创建document对象
        this.document = createDocument(new InputSource(inputStream));
    }

    private void commonConstructor(boolean validation, Properties variables, EntityResolver entityResolver) {
        this.validation = validation;
        this.entityResolver = entityResolver;
        this.variables = variables;

        // 创建XPath对象
        XPathFactory xPathFactory = XPathFactory.newInstance();
        this.xpath = xPathFactory.newXPath();
    }

    public XNode evalNode(String expression) {
        return evalNode(document, expression);
    }

    public XNode evalNode(Object root, String expression) {
        // 解析Node对象
        Node node = (Node) evaluate(expression, root, XPathConstants.NODE);
        if (null == node) {
            return null;
        }
        // 封装并返回ibatis封装的XNode对，这里已经对变量描述符做了调整，具体在GenericTokenParser中
        return new XNode(this, node, variables);
    }

    /**
     * 从root对象中解析expression对应标签，返回returnType实体对象
     *
     * @param expression
     * @param root
     * @param returnType
     * @return
     */
    private Object evaluate(String expression, Object root, QName returnType) {
        try {
            // 解析标签返回对象jdk方法
            return xpath.evaluate(expression, root, returnType);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("构建xpath出错.  原因: " + e, e);
        }
    }

    private Document createDocument(InputSource inputSource) {
        try {
            // 初始化一个XML解析工厂
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setValidating(validation);

            factory.setNamespaceAware(false);
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(false);
            factory.setCoalescing(false);
            factory.setExpandEntityReferences(true);

            // 创建一个DocumentBuilder实例
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(entityResolver);

            builder.setErrorHandler(new ErrorHandler() {
                @Override
                public void error(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    // NOP
                }
            });
            return builder.parse(inputSource);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("创建document出错.  原因: " + e, e);
        }

    }
}
