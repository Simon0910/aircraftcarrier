package com.aircraftcarrier.framework.tookit;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author lzp
 */
@Slf4j
public class StringUtil {
    /**
     * EMPTY
     */
    private static final String EMPTY = "";

    /**
     * StringUtil
     */
    private StringUtil() {
    }

    /**
     * <pre>
     * StringUtil.isBlank(null)      = true
     * StringUtil.isBlank("")        = true
     * StringUtil.isBlank(" ")       = true
     * StringUtil.isBlank("bob")     = false
     * StringUtil.isBlank("  bob  ") = false
     * </pre>
     *
     * @see org.apache.commons.lang3.StringUtils#isBlank(CharSequence)
     */
    public static boolean isBlank(CharSequence str) {
        return !StringUtils.hasText(str);
    }

    public static boolean isNotBlank(CharSequence str) {
        return StringUtils.hasText(str);
    }

    /**
     * <pre>
     * StringUtil.isEmpty(null)      = true
     * StringUtil.isEmpty("")        = true
     * StringUtil.isEmpty(" ")       = false
     * StringUtil.isEmpty("bob")     = false
     * StringUtil.isEmpty("  bob  ") = false
     * </pre>
     */
    public static boolean isEmpty(CharSequence str) {
        return !StringUtils.hasLength(str);
    }

    public static boolean isNotEmpty(CharSequence str) {
        return StringUtils.hasLength(str);
    }

    public static boolean contains(String str, String element) {
        return org.apache.commons.lang3.StringUtils.contains(str, element);
    }

    public static String trim(String str) {
        return StringUtils.trimWhitespace(str);
    }

    public static String trimAllWhitespace(String str) {
        return StringUtils.trimAllWhitespace(str);
    }

    public static boolean endsWith(String str, String suffix) {
        if (EMPTY.equals(suffix)) {
            log.error("endsWith suffix is ''");
        }
        return org.apache.commons.lang3.StringUtils.endsWith(str, suffix);
    }

    public static boolean endsWithIgnoreCase(String str, String suffix) {
        if (EMPTY.equals(suffix)) {
            log.error("endsWithIgnoreCase suffix is ''");
        }
        return org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(str, suffix);
    }

    public static List<String> split(String str, String separator) {
        String[] split = org.apache.commons.lang3.StringUtils.split(str, separator);
        if (split == null || split.length == 0) {
            return new ArrayList<>();
        }
        // 去前后空格，去重复，去空字符
        return Arrays.stream(split)
                .map(org.apache.commons.lang3.StringUtils::trim)
                .distinct()
                .filter(StringUtil::isNotBlank)
                .toList();
    }

    public static String distinct(String str, String separator) {
        return org.apache.commons.lang3.StringUtils.join(split(str, separator), separator);
    }

    public static String join(Collection<? extends CharSequence> collection, String separator) {
        return org.apache.commons.lang3.StringUtils.join(collection, separator);
    }

    public static String join(String separator, String... string) {
        if (string == null) {
            return EMPTY;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0, len = string.length - 1; i <= len; i++) {
            builder.append(string[i]);
            if (i < len) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        System.out.println("-------join----------");
        System.out.println(join(",", "123", null));
        System.out.println(join(Lists.newArrayList("a", "b"), null));
        String str = "    123, 123 , ,a,,a ,    a";
        System.out.println("-------distinct----------");
        System.out.println(distinct(str, ","));
        System.out.println(distinct(null, null));
        System.out.println("-------split----------");
        System.out.println(split(str, ","));
        System.out.println(split(null, null));

    }
}
