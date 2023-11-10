package com.jingxc.ibatis.parsing;

import java.util.Properties;

public class PropertyParser {

    private static final String KEY_PREFIX = "com.jingxc.ibatis.parsing.PropertyParser.";

    public static final String KEY_ENABLE_DEFAULT_VALUE = KEY_PREFIX + "enable-default-value";

    public static final String KEY_DEFAULT_VALUE_SEPARATOR = KEY_PREFIX + "default-value-separator";

    private static final String ENABLE_DEFAULT_VALUE = "false";

    private static final String DEFAULT_VALUE_SEPARATOR = ":";

    /**
     * 对比获取属性值(默认值，传入值)
     *
     * @param nodeValue
     * @param variables
     * @return
     */
    public static String parse(String nodeValue, Properties variables) {
        // 拦截器
        VariableTokenHandler h = new VariableTokenHandler(variables);
        // 去除变量描述字符
        GenericTokenParser genericTokenParser = new GenericTokenParser("${", "}", h);

        return genericTokenParser.parse(nodeValue);
    }

    private static class VariableTokenHandler implements TokenHandler {

        private final Properties variables;
        private final boolean enableDefaultValue;

        private final String defaultValueSeparator;

        public VariableTokenHandler(Properties variables) {
            this.variables = variables;
            this.enableDefaultValue = Boolean.parseBoolean(getPropertyValue(KEY_ENABLE_DEFAULT_VALUE, ENABLE_DEFAULT_VALUE));
            this.defaultValueSeparator = getPropertyValue(KEY_DEFAULT_VALUE_SEPARATOR, DEFAULT_VALUE_SEPARATOR);
        }

        private String getPropertyValue(String key, String defaultValue) {
            return variables == null ? defaultValue : variables.getProperty(key, defaultValue);
        }

        /**
         * 在对属性集赋值以后就会修改变量描述符
         *
         * @param content
         * @return
         */
        @Override
        public String handleToken(String content) {
            System.out.println("进入拦截器：" + content);
            // 对变量描述符进行替换，并从属性集中获取真实字段
            if (variables != null) {
                String key = content;
                if (enableDefaultValue) {
                    final int separator = content.indexOf(defaultValueSeparator);
                    String defaultValue = null;
                    if (separator >= 0) {
                        key = content.substring(0, separator);
                        // <property name="username" value="${username:ut_user}"/>
                        defaultValue = content.substring(separator + defaultValueSeparator.length());
                    }
                    if (defaultValue != null) {
                        return variables.getProperty(key, defaultValue);
                    }
                }
                if (variables.contains(key)) {
                    return variables.getProperty(key);
                }
            }
            return "${" + content + "}";
        }
    }
}
