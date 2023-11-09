package com.jingxc.ibatis.parsing;

public class GenericTokenParser {

    private final String openToken;
    private final String closeToken;
    private final TokenHandler handler;

    public GenericTokenParser(String openToken, String closeToken, TokenHandler h) {
        this.openToken = openToken;
        this.closeToken = closeToken;
        this.handler = h;
    }

    public String parse(String text) {
        if (null == text) {
            return "";
        }
        int start = text.indexOf(openToken);

        // 如果text中不包含openToken,则直接返回，否则需要预处理，mysql中变量${}
        if (start == -1) {
            return text;
        }

        // 转换为字符集
        char[] src = text.toCharArray();
        // 偏移量
        int offset = 0;

        final StringBuilder builder = new StringBuilder();
        StringBuilder expression = null;
        do {
            if (start > 0 && src[start - 1] == '\\') {
                // 删除openToken前的转译字符，如\${==>${
                builder.append(src, offset, start - offset - 1).append(openToken);
                // 使偏移量移至openToken后面
                offset = start + openToken.length();
            } else {
                if (expression == null) {
                    expression = new StringBuilder();
                } else {
                    expression.setLength(0);
                }

                builder.append(src, offset, start - offset);
                offset = start + openToken.length();

                // 返回此字符串中第一次出现指定子字符串的索引，从指定索引开始。
                // 处理完openToken，接着处理closeToken
                int end = text.indexOf(closeToken, offset);
                while (end > -1) {
                    if (end > offset && src[end - 1] == '\\') {
                        // 仅仅删除转义字符
                        expression.append(src, offset, end - offset - 1).append(closeToken);
                        offset = end + closeToken.length();
                        // 获取end的相对位置不变，只是为了去除转译字符，用于与openToken配对只能是，text的第一个
                        end = text.indexOf(closeToken, offset);
                    } else {
                        // 遇见配对的直接结束循环
                        expression.append(src, offset, end - offset);
                        break;
                    }
                }
                if (end == -1) {
                    // 没有发现closeToken
                    builder.append(src, start, src.length - start);
                    offset = src.length;
                } else {
                    // 发现closeToken，拼接在 while (end > -1)循环中存储的中间字符
                    builder.append(handler.handleToken(expression.toString()));
                    offset = end + closeToken.length();
                }
            }
            start = text.indexOf(openToken, offset);
        } while (start > -1);
        if (offset < src.length) {
            builder.append(src, offset, src.length - offset);
        }
        return builder.toString();
    }
}
