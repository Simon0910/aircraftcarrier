package com.aircraftcarrier.framework.tookit;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 统计时间执行时长
 *
 * @author zhipengliu
 * @date 2022/7/28
 * @since 1.0
 */
public class TimeLogUtil {

    private static final Logger logger = LoggerFactory.getLogger(TimeLogUtil.class);

    /**
     * LogTimeUtil
     */
    private TimeLogUtil() {
    }

    private static String wrapElapsedTime(long duration) {
        return "Elapsed time: [ " + duration + " ] ms ";
    }

    /**
     * 获取开始时间
     *
     * @return long
     */
    public static long startTime() {
        return System.currentTimeMillis();
    }


    /**
     * 获取时钟
     *
     * @return Stopwatch
     */
    public static Stopwatch createStopwatch() {
        return Stopwatch.createUnstarted();
    }


    /**
     * 获取 执行时间
     *
     * @return long
     */
    public static long elapsedTime(long startTime) {
        return System.currentTimeMillis() - startTime;
    }

    public static void logElapsedTime(long startTime) {
        logger.info(formatLogMessage(wrapElapsedTime(elapsedTime(startTime))));
    }

    public static void logElapsedTime(String message, long startTime) {
        logger.info(formatLogMessage(message + " - " + wrapElapsedTime(elapsedTime(startTime))));
    }

    public static void logElapsedTime(Stopwatch stopwatch) {
        if (!stopwatch.isRunning()) {
            logger.info(formatLogMessage("stopwatch is not start"));
        } else {
            logger.info(formatLogMessage(wrapElapsedTime(stopwatch.elapsed().toMillis())));
            stopwatch.reset();
        }
    }

    public static void logElapsedTime(String message, Stopwatch stopwatch) {
        if (!stopwatch.isRunning()) {
            logger.info(formatLogMessage("stopwatch is not start"));
        } else {
            logger.info(formatLogMessage(wrapElapsedTime(stopwatch.elapsed().toMillis())));
            stopwatch.reset();
        }
    }

    private static String formatLogMessage(String message) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[3];
        return caller.getMethodName() + "(" + caller.getFileName() + ":" + caller.getLineNumber() + ") - " +
                Log.getFullTid() +
                message;
    }
}
