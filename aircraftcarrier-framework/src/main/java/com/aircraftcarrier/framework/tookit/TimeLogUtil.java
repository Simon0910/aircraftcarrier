package com.aircraftcarrier.framework.tookit;

import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;

/**
 * 统计时间执行时长
 *
 * @author zhipengliu
 * @date 2022/7/28
 * @since 1.0
 */
public class TimeLogUtil {

    /**
     * LogTimeUtil
     */
    private TimeLogUtil() {
    }

    /**
     * 获取开始时间
     *
     * @return long
     */
    public static long beginTime() {
        return System.currentTimeMillis();
    }

    /**
     * 获取时钟执行时间
     *
     * @return long
     */
    public static long endTime(long startTime) {
        return System.currentTimeMillis() - startTime;
    }

    public static String endTimeStr(long startTime) {
        return endTime(startTime) + "ms";
    }

    /**
     * 获取时钟
     *
     * @return Stopwatch
     */
    public static Stopwatch startStopwatchTime() {
        return Stopwatch.createStarted();
    }

    /**
     * 获取时钟执行时间
     * 结束使用 {@link #endStopwatchTime(Stopwatch)} 方法
     *
     * @param stopwatch stopwatch
     * @return long
     */
    public static long getDuration(Stopwatch stopwatch) {
        stopwatch.stop();
        long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        stopwatch.reset();
        stopwatch.start();
        return elapsed;
    }

    public static String getDurationStr(Stopwatch stopwatch) {
        return getDuration(stopwatch) + "ms";
    }

    /**
     * 重新计时
     *
     * @param stopwatch stopwatch
     */
    public static void restartStopwatch(Stopwatch stopwatch) {
        stopwatch.reset();
        stopwatch.start();
    }

    /**
     * 获取时钟执行时间
     *
     * @param stopwatch stopwatch
     * @return long
     */
    public static long endStopwatchTime(Stopwatch stopwatch) {
        stopwatch.stop();
        return stopwatch.elapsed(TimeUnit.MILLISECONDS);
    }

    public static String endStopwatchTimeStr(Stopwatch stopwatch) {
        return endStopwatchTime(stopwatch) + "ms";
    }

}
