package com.aircraftcarrier.framework.tookit;

/**
 * StrUtil
 *
 * @author lzp
 * @since 2021-12-06
 */
public class StrUtil {
    private static final char UNDER_LINE = '_';

    private StrUtil() {
    }

    /**
     * 下划线转驼峰
     *
     * @param name name
     * @return String
     */
    public static String toCamelCase(String name) {
        if (null == name || name.length() == 0) {
            return null;
        }

        if (!contains(name, UNDER_LINE)) {
            return name;
        }

        int length = name.length();
        StringBuilder sb = new StringBuilder(length);
        boolean underLineNextChar = false;

        for (int i = 0; i < length; ++i) {
            char c = name.charAt(i);
            if (c == UNDER_LINE) {
                underLineNextChar = true;
            } else if (underLineNextChar) {
                sb.append(Character.toUpperCase(c));
                underLineNextChar = false;
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    public static boolean contains(String str, char searchChar) {
        return str.indexOf(searchChar) >= 0;
    }
}
