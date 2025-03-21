package com.aircraftcarrier.framework.exceltask.abnoraml;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import com.aircraftcarrier.framework.concurrent.ExecutorUtil;
import com.aircraftcarrier.framework.exceltask.Task;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * AutoCheckAbnormalScheduler
 *
 * @author zhipengliu
 * @date 2023/4/1
 * @since 1.0
 */
@Slf4j
public class AutoCheckAbnormalScheduler {

    /**
     * Task
     */
    private final Task<?> task;

    /**
     * 检测是否连续报错，则停止任务
     */
    private Map<String, String> abnormalMap;

    /**
     * 统计错误记录
     */
    private ThreadPoolExecutor statisticsAbnormalExecutor;

    /**
     * timer
     */
    private ScheduledExecutorService checkAbnormalScheduler;

    public AutoCheckAbnormalScheduler(Task<?> task) {
        this.task = task;
        if (task.config().isEnableAbnormalAutoCheck()) {
            this.abnormalMap = Maps.newHashMapWithExpectedSize(task.config().getAbnormalSampleSize());
            this.statisticsAbnormalExecutor = newCachedThreadPoolWithDiscardOldestPolicy();
            this.checkAbnormalScheduler = Executors.newSingleThreadScheduledExecutor();
        }
    }

    /**
     * new pool
     *
     * @return ThreadPoolExecutor
     * @see Executors#newCachedThreadPool(ThreadFactory)
     */
    private ThreadPoolExecutor newCachedThreadPoolWithDiscardOldestPolicy() {
        return new ThreadPoolExecutor(
                // 指定大小
                0, 1, 30, TimeUnit.SECONDS,
                // 10000
                new LinkedBlockingQueue<>(10000),
                // factory
                ExecutorUtil.newNamedThreadFactory("abnormal-monitor"),
                //  Discard
                new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    public void startAutoCheckTimer() {
        // 是否启用 config
        if (!task.config().isEnableAbnormalAutoCheck()) {
            return;
        }

        checkAbnormalScheduler.scheduleAtFixedRate(() -> {
            try {
                log.debug("scheduler auto check...");
                doCheckForAbnormal();
            } catch (Exception e) {
                log.error("AutoCheckTimer scheduler error: {}", e.getMessage(), e);
                e.printStackTrace();
            }
        }, 0, task.config().getAutoCheckForAbnormalPeriod(), TimeUnit.MILLISECONDS);
    }


    /**
     * 检测任务是否异常
     */
    private void doCheckForAbnormal() {
        // 异常采样数
        int size = abnormalMap.size();
        if (size < task.config().getAbnormalSampleSize()) {
            return;
        }

        int[] arr = new int[size];
        synchronized (this) {
            int i = 0;
            for (String errorRecord : abnormalMap.keySet()) {
                arr[i] = Integer.parseInt(errorRecord.split(StrPool.UNDERLINE)[1]);
                i++;
            }
            abnormalMap.clear();
        }

        Arrays.sort(arr);

        // 是否100个以上连续报错
        if (!hasConsecutiveElements(arr, task.config().getConsecutiveAbnormalNum())) {
            return;
        }

        // 100个以上连续报错，停止任务
        log.info("AutoCheckTimer scheduler - 连续 {}个 报错，停止任务", task.config().getConsecutiveAbnormalNum());
        if (task.isAlive() && !task.isInterrupted()) {
            task.interrupt();
            // 可发送邮件提醒，任务停止
        }
    }

    /**
     * 判断数组中有consecutiveNum个连续的元素 （ChatGPT-4）
     *
     * @param arr            数组
     * @param consecutiveNum consecutiveNum个连续的元素
     * @return boolean
     */
    private boolean hasConsecutiveElements(int[] arr, int consecutiveNum) {
        int count = 1;
        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i] + 1 == arr[i + 1]) {
                count++;
                if (count == consecutiveNum) {
                    return true;
                }
            } else {
                count = 1;
            }
        }
        return false;
    }

    public void shutdown() {
        if (!task.config().isEnableAbnormalAutoCheck()) {
            return;
        }
        log.info("AutoCheckTimer scheduler finish shutdown...");
        checkAbnormalScheduler.shutdown();
        statisticsAbnormalExecutor.shutdown();
        abnormalMap.clear();
    }

    public void putAbnormal(String sheetNoRowNo) {
        if (!task.config().isEnableAbnormalAutoCheck()) {
            return;
        }
        statisticsAbnormalExecutor.execute(() -> {
            synchronized (this) {
                abnormalMap.put(sheetNoRowNo, CharSequenceUtil.EMPTY);
            }
        });
    }
}
