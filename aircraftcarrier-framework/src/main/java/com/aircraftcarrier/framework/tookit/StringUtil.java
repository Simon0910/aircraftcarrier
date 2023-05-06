package com.aircraftcarrier.framework.tookit;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lzp
 */
public class StringUtil {
    private static final String EMPTY = "";

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
        return !isNotBlank(str);
    }

    public static boolean isNotBlank(CharSequence str) {
        return hasText(str);
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
        return !isNotEmpty(str);
    }

    public static boolean isNotEmpty(CharSequence str) {
        return hasLength(str);
    }

    /**
     * StringUtils.hasText(null) = false
     * StringUtils.hasText("") = false
     * StringUtils.hasText(" ") = false
     * StringUtils.hasText("12345") = true
     * StringUtils.hasText(" 12345 ") = true
     * </pre>
     *
     * @see org.apache.commons.lang3.StringUtils#isNotBlank(CharSequence)
     */
    public static boolean hasText(CharSequence str) {
        return StringUtils.hasText(str);
    }

    /**
     * <pre class="code">
     * StringUtils.hasLength(null) = false
     * StringUtils.hasLength("") = false
     * StringUtils.hasLength(" ") = true
     * StringUtils.hasLength("Hello") = true
     * </pre>
     */
    public static boolean hasLength(CharSequence str) {
        return StringUtils.hasLength(str);
    }

    public static String trim(String str) {
        return StringUtils.trimWhitespace(str);
    }

    public static String trimAllWhitespace(String str) {
        return StringUtils.trimAllWhitespace(str);
    }

    public static String join(Collection<? extends CharSequence> collection, String separator) {
        if (CollectionUtils.isEmpty(collection)) {
            return EMPTY;
        }
        if (isEmpty(separator)) {
            separator = EMPTY;
        }
        return collection.stream().distinct().filter(StringUtil::isNotBlank).collect(Collectors.joining(separator));
    }

    public static List<String> split(String str, String separator) {
        if (str == null) {
            return new ArrayList<>();
        }
        if (isEmpty(separator)) {
            separator = EMPTY;
        }
        return Arrays.stream(str.split(separator)).distinct().filter(StringUtil::isNotBlank).toList();
    }

    public static boolean endsWithIgnoreCase(String str, String suffix) {
        return StringUtils.endsWithIgnoreCase(str, suffix);
    }

    public static String append(String str1, String str2, String separator) {
        if (isBlank(str1)) {
            str1 = EMPTY;
        }
        if (isBlank(str2)) {
            str2 = EMPTY;
        }
        if (isEmpty(separator)) {
            separator = EMPTY;
        }
        return str1 + separator + str2;
    }


    public static boolean contains(String str, String element) {
        if (hasText(str) && hasText(element)) {
            return str.contains(element);
        }
        return false;
    }
}
