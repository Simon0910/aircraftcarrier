package com.aircraftcarrier.framework.tookit;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * @author lzp
 */
public class DateTimeUtils {

    /**
     * STANDARD FORMAT
     */
    public static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * YYYY_MM_DD_FORMAT
     */
    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    /**
     * empty => ""
     */
    public static final String EMPTY = "";

    private DateTimeUtils() {
    }

    /**
     * 当前时间
     *
     * @return org.joda.time.DateTime
     */
    public static DateTime now() {
        return DateTime.now();
    }

    /**
     * millis->Date
     *
     * @param timeMillis timeMillis
     * @return java.util.Date
     */
    public static Date millisToDate(Long timeMillis) {
        DateTime date = new DateTime(timeMillis);
        return date.toDate();
    }


    /**
     * str->Date
     *
     * @param dateTimeStr dateTimeStr
     * @param formatStr   formatStr
     * @return java.util.Date
     */
    public static Date strToDate(String dateTimeStr, String formatStr) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(formatStr);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }


    /**
     * Date->str
     *
     * @param date      date
     * @param formatStr formatStr
     * @return java.lang.String
     */
    public static String dateToStr(Date date, String formatStr) {
        if (date == null) {
            return EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(formatStr);
    }


    /**
     * str->Date
     * yyyy-MM-dd HH:mm:ss
     *
     * @param dateTimeStr dateTimeStr
     * @return java.util.Date
     */
    public static Date strToDate(String dateTimeStr) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(STANDARD_FORMAT);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }


    /**
     * Date->str
     * yyyy-MM-dd HH:mm:ss
     *
     * @param date date
     * @return java.lang.String
     */
    public static String dateToStr(Date date) {
        if (date == null) {
            return EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(STANDARD_FORMAT);
    }


    /**
     * plusDays
     *
     * @param date date
     * @param days days
     * @return java.util.Date
     */
    public static Date plusDays(Date date, Integer days) {
        DateTime dateTime = new DateTime(date);
        return dateTime.plusDays(days).toDate();
    }


    /**
     * plusMonths
     *
     * @param date   date
     * @param months months
     * @return java.util.Date
     */
    public static Date plusMonths(Date date, Integer months) {
        DateTime dateTime = new DateTime(date);
        return dateTime.plusMonths(months).toDate();
    }


    /**
     * valid StartDate
     * 不能早于今天零点
     *
     * @param startDate startDate
     * @return java.util.Date
     */
    public static Date validStartDate(Date startDate) {
        DateTime startTime = new DateTime(startDate);
        DateTime todayStartTime = new DateTime().withTimeAtStartOfDay();
        if (startTime.isBefore(todayStartTime)) {
            return todayStartTime.toDate();
        }
        return startDate;
    }


    /**
     * 日期是现在之后
     *
     * @param date date
     * @return java.lang.Boolean
     */
    public static Boolean isAfterNow(Date date) {
        DateTime dateTime = new DateTime(date);
        return dateTime.isAfterNow();
    }

    /**
     * 日期是现在之前
     *
     * @param date date
     * @return java.lang.Boolean
     */
    public static Boolean isBeforeNow(Date date) {
        DateTime dateTime = new DateTime(date);
        return dateTime.isBeforeNow();
    }

}
