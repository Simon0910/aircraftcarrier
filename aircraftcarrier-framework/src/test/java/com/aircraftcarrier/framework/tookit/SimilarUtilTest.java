package com.aircraftcarrier.framework.tookit;

import com.google.common.base.Stopwatch;

/**
 * 类注释内容
 *
 * @author zhipengliu
 * @date 2023/9/2
 * @since 1.0
 */
public class SimilarUtilTest {

    public static void main(String[] args) throws InterruptedException {
        Stopwatch stopwatch0 = TimeLogUtil.startCreateStopwatch();
        stopwatch0.start();
        Stopwatch stopwatch = TimeLogUtil.startCreateStopwatch();

        stopwatch.start();
        double d = SimilarUtil.similar("福建省", "234福232建32省2");
        Thread.sleep(3000);
        TimeLogUtil.logElapsedTime(stopwatch);

        stopwatch.start();
        Thread.sleep(2000);
        Integer i = SimilarUtil.similarLevenshtein("福建省", "234福232建32省2");
        TimeLogUtil.logElapsedTime(stopwatch);

        // stopwatch.stop();

        Thread.sleep(500);
        System.out.println(d + " :: " + i);
        System.out.println(SimilarUtil.similar("福建省", "福建城") + " :: " + SimilarUtil.similarLevenshtein("福建省", "福建城"));
        System.out.println(SimilarUtil.similar("福建省", "湖建省") + " :: " + SimilarUtil.similarLevenshtein("福建省", "湖建省"));

        // 总耗时
        TimeLogUtil.logElapsedTime(stopwatch0);
    }
}
