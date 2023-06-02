package com.aircraftcarrier.framework.tookit;

import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * DateUtil
 *
 * @author liuzhipeng
 * @date: 2023/05/25 17:35
 */
public class DateUtil {

    /**
     * STANDARD_FORMAT
     */
    public static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter STANDARD_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder().appendPattern(STANDARD_FORMAT).toFormatter();
    private static final ZoneId ZONEID = ZoneId.systemDefault();
    private static final Map<String, DateTimeFormatter> FORMATTER_MAPPING_CACHE = new HashMap<>();

    private DateUtil() {
    }

    private static DateTimeFormatter getFormatter(String pattern) {
        return FORMATTER_MAPPING_CACHE.computeIfAbsent(pattern, k -> new DateTimeFormatterBuilder().appendPattern(pattern).toFormatter());
    }

    /**
     * 日期转为LocalDateTime
     *
     * @param date 日期
     * @return LocalDateTime
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        if (null == date) {
            return null;
        }
        return date.toInstant().atZone(ZONEID).toLocalDateTime();
    }

    /**
     * 日期转为LocalDate
     *
     * @param date 日期
     * @return LocalDateTime
     */
    public static LocalDate dateToLocalDate(Date date) {
        if (null == date) {
            return null;
        }
        return date.toInstant().atZone(ZONEID).toLocalDate();
    }

    /**
     * LocalDateTime转为日期
     *
     * @param localDateTime LocalDateTime
     * @return 日期
     */
    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZONEID).toInstant());
    }

    /**
     * LocalDate转为日期
     *
     * @param localDate localDate
     * @return 日期
     */
    public static Date localDateToDate(LocalDate localDate) {
        if (null == localDate) {
            return null;
        }
        return Date.from(localDate.atStartOfDay().atZone(ZONEID).toInstant());
    }

    public static String dateToStr(Date date) {
        if (date == null) {
            return null;
        }
        return dateToLocalDateTime(date).format(STANDARD_DATE_TIME_FORMATTER);
    }

    public static String dateToStr(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        return dateToLocalDateTime(date).format(getFormatter(pattern));
    }

    public static Date strToDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) {
            return null;
        }
        return localDateTimeToDate(LocalDateTime.parse(dateStr, STANDARD_DATE_TIME_FORMATTER));
    }

}
