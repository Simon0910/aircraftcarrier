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
public class LogTimeUtil {

    /**
     * LogTimeUtil
     */
    private LogTimeUtil() {
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
     * 获取时钟执行时间
     *
     * @return long
     */
    public static long endTime(long startTime) {
        return System.currentTimeMillis() - startTime;
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

}
