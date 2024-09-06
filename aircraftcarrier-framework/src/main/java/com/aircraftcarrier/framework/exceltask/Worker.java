package com.aircraftcarrier.framework.exceltask;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Worker
 *
 * @author zhipengliu
 * @date 2023/8/13
 * @since 1.0
 */
interface Worker<T extends AbstractExcelRow> {

    String start();

    /**
     * check
     *
     * @param t 行数据
     * @return true: 通过， false：忽略
     */
    boolean check(T t);

    /**
     * 如果中途抛出未知异常：默认当前一批任务全部失败处理，下次跳过执行
     * doWorker
     *
     * @param uploadDataList 一批数据
     */
    void doWork(LinkedList<T> uploadDataList);

    String stop();

    String reset();

    String resetSuccessSheetRow(String maxSuccessSheetRow) throws IOException;

    String settingFromWithEnd(String fromSheetRow, String endSheetRow);

    /**
     * 获取已经处理的行号
     *
     * @return rowNo
     */
    Integer obtainRowNoByProgressProcessed();

    Task<T> getTask();
}
