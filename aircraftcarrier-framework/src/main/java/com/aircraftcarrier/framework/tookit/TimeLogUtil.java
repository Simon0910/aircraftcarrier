package com.aircraftcarrier.framework.tookit;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 统计时间执行时长
 *
 * @author zhipengliu
 * @date 2022/7/28
 * @since 1.0
 */
@Slf4j
public class TimeLogUtil {

    /**
     * LogTimeUtil
     */
    private TimeLogUtil() {
    }

    private static String wrapperElapsedTime(long duration) {
        return "elapsed time: [ " + duration + " ] ms ";
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

    public static void endTimePrintln(long startTime) {
        System.out.println(wrapperElapsedTime(endTime(startTime)));
    }

    public static void endTimePrintln(String message, long startTime) {
        System.out.println(String.format(message.replaceAll("\\{\\}", "%s"), wrapperElapsedTime(endTime(startTime))));
    }

    public static void endTimeLog(String message, long startTime) {
        // 定位不到具体再哪一行打印的？
        log.info(String.format(message.replaceAll("\\{\\}", "%s"), wrapperElapsedTime(endTime(startTime))));
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

    public static void endStopwatchTimePrintln(Stopwatch stopwatch) {
        System.out.println(wrapperElapsedTime(endStopwatchTime(stopwatch)));
    }

}
