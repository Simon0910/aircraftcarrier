package com.aircraftcarrier.framework.tookit;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.CharUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author lzp
 */
public class StringUtil {
    private static final String EMPTY = "";

    private StringUtil() {

    }

    public static boolean isBlank(CharSequence str) {
        int length;
        if ((str == null) || ((length = str.length()) == 0)) {
            return true;
        }
        for (int i = 0; i < length; i++) {
            if (!CharUtil.isBlankChar(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(CharSequence str) {
        return !isBlank(str);
    }

    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence str) {
        return !isEmpty(str);
    }

    public static String trim(String str) {
        return org.springframework.util.StringUtils.trimAllWhitespace(str);
    }

    public static String join(Collection<? extends CharSequence> collection, String separator) {
        if (CollUtil.isEmpty(collection) || isBlank(separator)) {
            return EMPTY;
        }
        return collection.stream().distinct().filter(StringUtil::isNotBlank).collect(Collectors.joining(separator));
    }

    public static List<String> split(String str, String separator) {
        if (isBlank(str) || isBlank(separator)) {
            return new ArrayList<>();
        }
        return Stream.of(str.split(separator)).distinct().filter(StringUtil::isNotBlank).collect(Collectors.toList());
    }

    public static boolean endsWith(String str, String suffix) {
        return endsWith(str, suffix, false);
    }

    public static String camelToHyphen(String input) {
        return wordsToHyphenCase(wordsAndHyphenAndCamelToConstantCase(input));
    }

    private static boolean endsWith(String str, String suffix, boolean ignoreCase) {
        if (str != null && suffix != null) {
            if (suffix.length() > str.length()) {
                return false;
            } else {
                int strOffset = str.length() - suffix.length();
                return str.regionMatches(ignoreCase, strOffset, suffix, 0, suffix.length());
            }
        } else {
            return str == null && suffix == null;
        }
    }

    private static String wordsAndHyphenAndCamelToConstantCase(String input) {
        StringBuilder buf = new StringBuilder();
        char previousChar = ' ';
        char[] chars = input.toCharArray();
        char[] var4 = chars;
        int var5 = chars.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            char c = var4[var6];
            boolean isUpperCaseAndPreviousIsLowerCase = Character.isLowerCase(previousChar) && Character.isUpperCase(c);
            boolean previousIsWhitespace = Character.isWhitespace(previousChar);
            boolean lastOneIsNotUnderscore = buf.length() > 0 && buf.charAt(buf.length() - 1) != '_';
            boolean isNotUnderscore = c != '_';
            if (!lastOneIsNotUnderscore || !isUpperCaseAndPreviousIsLowerCase && !previousIsWhitespace) {
                if (Character.isDigit(previousChar) && Character.isLetter(c)) {
                    buf.append('_');
                }
            } else {
                buf.append("_");
            }

            if (shouldReplace(c) && lastOneIsNotUnderscore) {
                buf.append('_');
            } else if (!Character.isWhitespace(c) && (isNotUnderscore || lastOneIsNotUnderscore)) {
                buf.append(Character.toUpperCase(c));
            }

            previousChar = c;
        }

        if (Character.isWhitespace(previousChar)) {
            buf.append("_");
        }

        return buf.toString();
    }

    private static boolean shouldReplace(char c) {
        return c == '.' || c == '_' || c == '-';
    }

    private static String wordsToHyphenCase(String s) {
        StringBuilder buf = new StringBuilder();
        char lastChar = 'a';
        char[] var3 = s.toCharArray();
        int var4 = var3.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            char c = var3[var5];
            if (Character.isWhitespace(lastChar) && !Character.isWhitespace(c) && '-' != c && buf.length() > 0 && buf.charAt(buf.length() - 1) != '-') {
                buf.append("-");
            }

            if ('_' == c) {
                buf.append('-');
            } else if ('.' == c) {
                buf.append('-');
            } else if (!Character.isWhitespace(c)) {
                buf.append(Character.toLowerCase(c));
            }

            lastChar = c;
        }

        if (Character.isWhitespace(lastChar)) {
            buf.append("-");
        }

        return buf.toString();
    }
}
