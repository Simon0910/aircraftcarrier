package com.aircraftcarrier.framework.exceltask.refresh;

import com.aircraftcarrier.framework.exceltask.AbstractExcelRow;

import java.util.Collections;
import java.util.Map;

/**
 * @author liuzhipeng
 * @since 2024/9/4
 */
public interface RefreshStrategy {

    /**
     * 初始化
     */
    void preHandle() throws Exception;

    /**
     * 返回上一次处理成功的最大的位置
     *
     * @return maxSuccessSnapshotPosition:
     * @throws Exception 执行任务之前抛出异常
     */
    String loadSuccessMapSnapshot() throws Exception;

    /**
     * 返回上一次处理失败的所有记录
     *
     * @return Map<String, String>: key:position, value:null
     * @throws Exception 执行任务之前抛出异常
     */
    Map<String, String> loadErrorMapSnapshot() throws Exception;

    /**
     * 定时持久化结果快照
     */
    void startRefreshSnapshot();

    /**
     * 记录excel 读取数
     */
    void incrementReadNum();

    /**
     * 记录错误行到内存
     */
    void recordErrorRowPosition(AbstractExcelRow row);

    /**
     * 记录成功结行到内存
     */
    void recordSuccessRowPosition(AbstractExcelRow row, int successSize);

    /**
     * 关闭资源
     */
    void shutdown();


}
