package com.aircraftcarrier.framework.exceltask.refresh;

import cn.hutool.core.text.CharSequenceUtil;
import com.aircraftcarrier.framework.concurrent.ThreadUtil;
import com.aircraftcarrier.framework.exceltask.AbstractExcelRow;
import com.aircraftcarrier.framework.exceltask.ExcelTaskException;
import com.aircraftcarrier.framework.exceltask.ExcelUtil;
import com.aircraftcarrier.framework.exceltask.TaskConfig;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author liuzhipeng
 * @since 2024/9/4
 */
@Slf4j
public abstract class AbstractRefreshStrategy implements RefreshStrategy {

    /**
     * config
     */
    protected final TaskConfig config;

    /**
     * 记录各个线程执行到第几行
     * key:线程号， value:sheetNo.rowNo
     * 每隔3秒记录一下
     */
    private final HashMap<String, String> successMap;

    /**
     * 记录错误 刷新错误 并发锁
     */
    private final Object errorLock = new Object();

    /**
     * 错误的记录
     * key:线程号， value:sheetNo.rowNo
     * 每隔3秒记录一下
     */
    private final HashMap<String, String> errorMap = new HashMap<>();

    /**
     * 定时刷新 map快照 errorMap快照
     * 任务重启后从最新位置开始
     */
    private final ScheduledExecutorService refreshSnapshotScheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * 统计处理成功的总数
     */
    private final LongAdder successNum = new LongAdder();
    /**
     * excel invoke num
     */
    private int totalReadNum;
    /**
     * 统计失败数
     */
    private int failNum;


    AbstractRefreshStrategy(TaskConfig config) {
        this.config = config;
        this.config.setRefreshStrategy(this);
        this.successMap = Maps.newHashMapWithExpectedSize(config.getThreadNum());
    }


    @Override
    public void startRefreshSnapshot() {
        refreshSnapshotScheduler.scheduleAtFixedRate(() -> {
            try {
                // 刷新错误记录
                refreshErrorMapSnapshot();
                // 刷新最新快照
                refreshSuccessMapSnapshot();
            } catch (Exception e) {
                log.error("init - refreshMapSnapshot error: {}", e.getMessage(), e);
            }
        }, 0, config.getRefreshSnapshotPeriod(), TimeUnit.MILLISECONDS);
    }

    private void refreshSuccessMapSnapshot() {
        if (successMap.isEmpty()) {
            log.debug("doRefresh successMap is Empty");
            return;
        }
        try {
            doRefreshSuccessMapSnapshot(successMap);
        } catch (Exception e) {
            throw new ExcelTaskException("doRefreshSuccessMapSnapshot error", e);
        } finally {
            log.info("doRefresh successNum {}", successNum);
        }
    }

    abstract void doRefreshSuccessMapSnapshot(Map<String, String> successMap) throws Exception;

    private void refreshErrorMapSnapshot() {
        if (errorMap.isEmpty()) {
            return;
        }
        synchronized (errorLock) {
            if (errorMap.isEmpty()) {
                return;
            }

            try {
                doRefreshErrorMapSnapshot(errorMap);
            } catch (Exception e) {
                throw new ExcelTaskException("doRefreshErrorMapSnapshot error", e);
            } finally {
                log.info("doRefresh errorMap size {}", errorMap.size());
                errorMap.clear();
            }
        }
    }

    abstract void doRefreshErrorMapSnapshot(Map<String, String> errorMap) throws Exception;

    @Override
    public void incrementReadNum() {
        totalReadNum++;
    }

    @Override
    public void recordErrorRowPosition(AbstractExcelRow row) {
        synchronized (errorLock) {
            // 不存在增加
            errorMap.computeIfAbsent(ExcelUtil.getRowPosition(row), k -> {
                failNum++;
                return CharSequenceUtil.EMPTY;
            });
        }
    }

    @Override
    public void recordSuccessRowPosition(AbstractExcelRow row, int successSize) {
        successMap.put(ThreadUtil.getThreadNo(), ExcelUtil.getRowPosition(row));
        successNum.add(successSize);
    }

    @Override
    public void shutdown() {
        try {
            // 停止刷新快照任务
            refreshSnapshotScheduler.shutdown();
            // 停止前刷新错误记录
            refreshErrorMapSnapshot();
            // 停止前刷新最新快照
            refreshSuccessMapSnapshot();
        } finally {
            log.info("finish shutdown - doRefresh totalReadNum: {} - successNum: {}, - failNum: {} = other: {}", totalReadNum, successNum, failNum, totalReadNum - successNum.intValue() - failNum);
            try {
                close();
            } catch (Exception e) {
                log.error("close error ", e);
            }
        }
    }

    abstract void close() throws Exception;
}
