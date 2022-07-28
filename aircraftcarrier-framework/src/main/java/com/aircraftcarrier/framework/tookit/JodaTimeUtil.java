package com.aircraftcarrier.framework.tookit;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public class JodaTimeUtil {

    /**
     * STANDARD_FORMAT
     */
    private static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * JodaTimeUtil
     */
    private JodaTimeUtil() {
    }

    /**
     * date类型 -> string类型
     *
     * @param date date
     * @return String
     */
    public static String dateToStr(Date date) {
        if (date == null) return StringPool.EMPTY;
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(STANDARD_FORMAT);
    }

    /**
     * date类型 -> string类型
     *
     * @param date          date
     * @param formatPattern formatPattern
     * @return String
     */
    public static String dateToStr(Date date, String formatPattern) {
        if (date == null) return StringPool.EMPTY;
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(formatPattern);
    }

    /**
     * string类型 -> date类型
     *
     * @param timeStr timeStr
     * @return Date
     */
    public static Date strToDate(String timeStr) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(STANDARD_FORMAT);
        DateTime dateTime = dateTimeFormatter.parseDateTime(timeStr);
        return dateTime.toDate();
    }

    /**
     * 判断date日期是否过期(与当前时刻比较)
     *
     * @param date date
     * @return boolean
     */
    public static boolean isTimeExpired(Date date) {
        if (null == date) return true;
        String timeStr = dateToStr(date);
        return isBeforeNow(timeStr);
    }

    /**
     * 判断date日期是否过期(与当前时刻比较)
     *
     * @param timeStr timeStr
     * @return boolean
     */
    public static boolean isTimeExpired(String timeStr) {
        if (StringUtil.isBlank(timeStr)) return true;
        return isBeforeNow(timeStr);
    }

    /**
     * 判断timeStr是否在当前时刻之前
     *
     * @param timeStr timeStr
     * @return boolean
     */
    private static boolean isBeforeNow(String timeStr) {
        DateTimeFormatter format = DateTimeFormat.forPattern(STANDARD_FORMAT);
        DateTime dateTime = DateTime.parse(timeStr, format);
        return dateTime.isBeforeNow();
    }

    /**
     * 日期加天数
     *
     * @param date date
     * @param days days
     * @return Date
     */
    public static Date plusDays(Date date, Integer days) {
        return plusOrMinusDays(date, days, 0);
    }

    /**
     * 日期减天数
     *
     * @param date date
     * @param days days
     * @return Date
     */
    public static Date minusDays(Date date, Integer days) {
        return plusOrMinusDays(date, days, 1);
    }

    /**
     * 加减天数
     *
     * @param date date
     * @param days days
     * @param type 0:加天数 1:减天数
     * @return Date
     */
    private static Date plusOrMinusDays(Date date, Integer days, Integer type) {
        if (null == date) return null;
        days = null == days ? 0 : days;

        DateTime dateTime = new DateTime(date);
        if (type == 0) {
            dateTime = dateTime.plusDays(days);
        } else {
            dateTime = dateTime.minusDays(days);
        }

        return dateTime.toDate();
    }

    /**
     * 日期加分钟
     *
     * @param date    date
     * @param minutes minutes
     * @return Date
     */
    public static Date plusMinutes(Date date, Integer minutes) {
        return plusOrMinusMinutes(date, minutes, 0);
    }

    /**
     * 日期减分钟
     *
     * @param date    date
     * @param minutes minutes
     * @return Date
     */
    public static Date minusMinutes(Date date, Integer minutes) {
        return plusOrMinusMinutes(date, minutes, 1);
    }

    /**
     * 加减分钟
     *
     * @param date    date
     * @param minutes minutes
     * @param type    0:加分钟 1:减分钟
     * @return Date
     */
    private static Date plusOrMinusMinutes(Date date, Integer minutes, Integer type) {
        if (null == date) return null;
        minutes = null == minutes ? 0 : minutes;

        DateTime dateTime = new DateTime(date);
        if (type == 0) {
            dateTime = dateTime.plusMinutes(minutes);
        } else {
            dateTime = dateTime.minusMinutes(minutes);
        }

        return dateTime.toDate();
    }

    /**
     * 日期加月份
     *
     * @param date   date
     * @param months months
     * @return Date
     */
    public static Date plusMonths(Date date, Integer months) {
        return plusOrMinusMonths(date, months, 0);
    }

    /**
     * 日期减月份
     *
     * @param date   date
     * @param months months
     * @return Date
     */
    public static Date minusMonths(Date date, Integer months) {
        return plusOrMinusMonths(date, months, 1);
    }

    /**
     * 加减月份
     *
     * @param date   date
     * @param months months
     * @param type   0:加月份 1:减月份
     * @return Date
     */
    private static Date plusOrMinusMonths(Date date, Integer months, Integer type) {
        if (null == date) return null;
        months = null == months ? 0 : months;

        DateTime dateTime = new DateTime(date);
        if (type == 0) {
            dateTime = dateTime.plusMonths(months);
        } else {
            dateTime = dateTime.minusMonths(months);
        }

        return dateTime.toDate();
    }

    /**
     * 判断target是否在开始和结束时间之间
     *
     * @param target    target
     * @param startTime startTime
     * @param endTime   endTime
     * @return boolean
     */
    public static boolean isBetweenStartAndEndTime(Date target, Date startTime, Date endTime) {
        if (null == target || null == startTime || null == endTime) {
            return false;
        }

        DateTime dateTime = new DateTime(target);
        return dateTime.isAfter(startTime.getTime()) && dateTime.isBefore(endTime.getTime());
    }

}

