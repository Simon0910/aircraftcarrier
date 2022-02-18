package com.aircraftcarrier.framework.support.validation;

import cn.hutool.core.util.StrUtil;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 校验工具类
 *
 * @author yudao
 */
public class ValidationUtils {

    private static final Pattern PATTERN_URL = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

    private static final Pattern PATTERN_XML_NCNAME = Pattern.compile("[a-zA-Z_][\\-_.0-9_a-zA-Z$]*");

    public static boolean isMobile(String mobile) {
        return StrUtil.length(mobile) == 11;
        // TODO 后面完善手机校验
    }

    public static boolean isURL(String url) {
        return StringUtils.hasText(url)
                && PATTERN_URL.matcher(url).matches();
    }

    public static boolean isXmlNCName(String str) {
        return StringUtils.hasText(str)
                && PATTERN_XML_NCNAME.matcher(str).matches();
    }

}
