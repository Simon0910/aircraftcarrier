package com.aircraftcarrier.framework.exceltask;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
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
 * AutoCheckTimer
 *
 * @author zhipengliu
 * @date 2023/4/1
 * @since 1.0
 */
@Slf4j
public class AutoCheckAbnormalScheduler {

    /**
     * config
     */
    private final TaskConfig config;
    /**
     * 检测是否连续报错，则停止任务
     */
    private final Map<String, String> abnormalMap;
    /**
     * pool
     */
    private final ThreadPoolExecutor threadPoolExecutor = newCachedThreadPoolWithDiscardOldestPolicy();
    /**
     * timer
     */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


    public AutoCheckAbnormalScheduler(TaskConfig config) {
        this.config = config;
        this.abnormalMap = Maps.newHashMapWithExpectedSize(config.getAbnormalSampleSize());
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
                ThreadPoolUtil.newThreadFactory("abnormal-monitor"),
                //  Discard
                new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    public void putAbnormal(String sheetNoRowNo) {
        threadPoolExecutor.execute(() -> {
            if (config.isEnableAbnormalAutoCheck()) {
                abnormalMap.put(sheetNoRowNo, CharSequenceUtil.EMPTY);
            }
        });
    }

    /**
     * 检测任务是否异常
     */
    private void autoCheckForAbnormal() {
        // 异常采样数
        if (abnormalMap.size() < config.getAbnormalSampleSize()) {
            return;
        }

        int[] arr = new int[abnormalMap.size()];
        int i = 0;
        for (String errorRecord : abnormalMap.keySet()) {
            arr[i] = Integer.parseInt(errorRecord.split(StrPool.UNDERLINE)[1]);
            i++;
        }
        abnormalMap.clear();

        Arrays.sort(arr);

        // 是否100个以上连续报错
        if (!hasConsecutiveElements(arr, config.getConsecutiveAbnormalNum())) {
            return;
        }

        // 100个以上连续报错，停止任务
        log.info("AutoCheckTimer scheduler - 连续 {}个 报错，停止任务", config.getConsecutiveAbnormalNum());
        if (config.getTaskThread().isAlive() && !config.getTaskThread().isInterrupted()) {
            config.getTaskThread().interrupt();
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

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                log.info("scheduler auto check...");
                autoCheckForAbnormal();
            } catch (Exception e) {
                log.error("AutoCheckTimer scheduler error: {}", e.getMessage(), e);
                e.printStackTrace();
            }
        }, 0, config.getAutoCheckForAbnormalPeriod(), TimeUnit.MILLISECONDS);
    }

    public void stop() {
        log.info("AutoCheckTimer scheduler finish shutdown...");
        scheduler.shutdown();
        threadPoolExecutor.shutdownNow();
        abnormalMap.clear();
    }
}
