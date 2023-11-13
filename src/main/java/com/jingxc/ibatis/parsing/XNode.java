package com.jingxc.ibatis.parsing;

import org.w3c.dom.CharacterData;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class XNode {

    // XPath解析器对象
    private final XPathParser xPathParser;

    // Node节点对象
    private final Node node;

    // 属性集
    private final Properties variables;

    //  Node(配置文件)节点属性集
    private final Properties attributes;

    // 节点名称
    private final String name;

    private final String body;

    public XNode(XPathParser xPathParser, Node node, Properties variables) {
        // 解析器
        this.xPathParser = xPathParser;
        // 节点
        this.node = node;
        this.name = node.getNodeName();
        // 属性集
        this.variables = variables;

        // Node节点属性集
        this.attributes = parseAttribute(node);

        this.body = parseBody(node);

    }

    private String parseBody(Node node) {
        // 获取第一个不是null的bodyData
        String data = getBodyData(node);
        if (data == null) {
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);
                data = getBodyData(item);
                if (data != null) {
                    break;
                }

            }
        }
        return data;
    }

    private String getBodyData(Node node) {
        if (node.getNodeType() == Node.CDATA_SECTION_NODE || node.getNodeType() == Node.TEXT_NODE) {
            String data = ((CharacterData) node).getData();
            data = PropertyParser.parse(data, variables);
            return data;
        }
        return null;
    }

    /**
     * 获取并创建Node节点属性集
     *
     * @param node
     * @return
     */
    private Properties parseAttribute(Node node) {
        // 创建属性集
        Properties properties = new Properties();
        // 获取节点属性集
        NamedNodeMap nameNodeMap = node.getAttributes();
        if (nameNodeMap != null) {
            // 循环node节点属性封装Properties属性集
            for (int i = 0; i < nameNodeMap.getLength(); i++) {
                Node n = nameNodeMap.item(i);
                // 获取节点属性值，并处理变量描述符
                String value = PropertyParser.parse(n.getNodeValue(), variables);

                // 节点属性集赋值
                properties.put(n.getNodeName(), value);
            }
        }
        return properties;
    }

    public XNode evalNode(String expression) {
        return xPathParser.evalNode(node, expression);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        toString(builder, 0);
        return builder.toString();
    }

    private void toString(StringBuilder builder, int level) {
        builder.append("<");
        builder.append(name);
        for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
            builder.append(" ");
            builder.append(entry.getKey());
            builder.append("=\"");
            builder.append(entry.getValue());
            builder.append("\"");
        }
        List<XNode> children = getChildren();
        if (!children.isEmpty()) {
            builder.append(">\n");
            for (XNode child : children) {
                indent(builder, level + 1);
                child.toString(builder, level + 1);
            }
            indent(builder, level);
            builder.append("</");
            builder.append(name);
            builder.append(">");
        } else if (body != null) {
            builder.append(">");
            builder.append(body);
            builder.append("</");
            builder.append(name);
            builder.append(">");
        } else {
            builder.append("/>");
            indent(builder, level);
        }
        builder.append("\n");
    }

    private void indent(StringBuilder builder, int level) {
        for (int i = 0; i < level; i++) {
            builder.append("    ");
        }
    }

    public List<XNode> getChildren() {
        List<XNode> children = new ArrayList<>();
        NodeList nodeList = node.getChildNodes();
        if (nodeList != null) {
            for (int i = 0, n = nodeList.getLength(); i < n; i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    children.add(new XNode(xPathParser, node, variables));
                }
            }
        }
        return children;
    }

    /**
     * 获取子节点返回Properties
     *
     * @return
     */
    public Properties getChildrenAsProperties() {
        Properties properties = new Properties();
        // 获取子节点并通过name，value返回Properties
        for (XNode child : getChildren()) {
            String name = child.getStringAttribute("name");
            String value = child.getStringAttribute("value");
            if (name != null && value != null) {
                properties.setProperty(name, value);
            }
        }
        return properties;
    }

    public String getStringAttribute(String name) {
        return getStringAttribute(name, (String) null);
    }

    public String getStringAttribute(String name, String def) {
        String value = attributes.getProperty(name);
        return value == null ? def : value;
    }
}
