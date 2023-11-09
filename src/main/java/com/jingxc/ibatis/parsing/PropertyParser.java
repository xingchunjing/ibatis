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

        @Override
        public String handleToken(String content) {
            return null;
        }
    }
}
