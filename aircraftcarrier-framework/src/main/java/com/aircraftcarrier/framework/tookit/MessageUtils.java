package com.aircraftcarrier.framework.tookit;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * 国际化
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
public class MessageUtils {
    private static final MessageSource MESSAGE_SOURCE;

    static {
        MESSAGE_SOURCE = (MessageSource) SpringContextUtils.getBean("messageSource");
    }

    private MessageUtils() {
    }

    public static String getMessage(int code) {
        return getMessage(code, new String[0]);
    }

    public static String getMessage(int code, String... params) {
        return MESSAGE_SOURCE.getMessage(code + "", params, LocaleContextHolder.getLocale());
    }
}
