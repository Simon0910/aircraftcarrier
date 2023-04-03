package com.aircraftcarrier.framework.exceltask;

import java.util.LinkedList;

/**
 * @author zhipengliu
 */
public interface Worker<T> {

    /**
     * check
     *
     * @param t 行数据
     * @return true: 通过， false：忽略
     */
    default boolean check(T t) {
        return true;
    }

    /**
     * 如果中途抛出未知异常：默认当前一批任务全部失败处理，下次跳过执行
     * doWorker
     *
     * @param uploadDataList 一批数据
     */
    void doWorker(LinkedList<T> uploadDataList);
}
