package com.aircraftcarrier.framework.exceltask;

import com.aircraftcarrier.framework.concurrent.ExecutorUtil;
import com.aircraftcarrier.framework.exceltask.abnoraml.AutoCheckAbnormalScheduler;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelAnalysisStopException;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.read.metadata.holder.ReadSheetHolder;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * 处理 excel task
 * <a href="https://cloud.tencent.com/developer/news/783592">...</a>
 *
 * @author zhipengliu
 */
@Slf4j
public class ExcelReadListener<T extends AbstractExcelRow> implements ReadListener<T> {

    private final TaskConfig config;
    private final ExecutorService executorService;
    private final Worker<T> worker;

    /**
     * 批次处理，可重复使用容器
     */
    private final LinkedList<T> batchContainer;

    /**
     * 上一次成功的最大行号
     * sheetNo_rowNo
     */
    private final String maxSuccessSnapshotPosition;

    /**
     * 上一次失败的记录
     */
    private final Map<String, String> errorMapSnapshot;

    /**
     * 连续执行异常 自动停止任务
     */
    private final AutoCheckAbnormalScheduler autoCheckAbnormalScheduler;


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

    private final Statistics statistics = new Statistics();


    ExcelReadListener(Worker<T> worker, TaskConfig config) throws Exception {
        this.config = config;
        this.batchContainer = new LinkedList<>();
        this.executorService = ExecutorUtil.newCachedThreadPoolBlock(config.getThreadNum(), config.getPoolName());
        this.worker = worker;

        this.config.getRefreshStrategy().startRefreshSnapshot(this::getStatisticsSupplier);
        this.maxSuccessSnapshotPosition = this.config.getRefreshStrategy().loadSuccessMapSnapshot();
        this.errorMapSnapshot = this.config.getRefreshStrategy().loadErrorMapSnapshot();

        this.autoCheckAbnormalScheduler = new AutoCheckAbnormalScheduler(worker.getTask());
        this.autoCheckAbnormalScheduler.startAutoCheckTimer();
    }

    private Statistics getStatisticsSupplier() {
        statistics.setFailNum(failNum);
        statistics.setSuccessNum(successNum.intValue());
        statistics.setTotalReadNum(totalReadNum);
        return statistics;
    }


    @Override
    public void onException(Exception e, AnalysisContext context) throws Exception {
        ReadSheetHolder readSheetHolder = context.readSheetHolder();
        Integer sheetNo = readSheetHolder.getSheetNo();
        Integer rowNo = getRowNo(readSheetHolder);
        Object currentRowAnalysisResult = context.readRowHolder().getCurrentRowAnalysisResult();

        if (isStopInvoke(e)) {
            log.info("doWork stop invoke - sheetNo:{}, rowNo:{}, msg: {}, uploadData: {}",
                    sheetNo, rowNo, e.getMessage(), JSON.toJSONString(currentRowAnalysisResult));
            ReadListener.super.onException(new ExcelAnalysisStopException(), context);
        }

        log.error("doWork error - onException - sheetNo:{}, rowNo:{}, errorMsg: {}, uploadData: {}",
                sheetNo, rowNo, e.getMessage(), JSON.toJSONString(currentRowAnalysisResult), e);
        ReadListener.super.onException(e, context);

    }

    private boolean isStopInvoke(Exception e) {
        return Thread.currentThread().isInterrupted() || e.getClass() == RejectedExecutionException.class || e.getClass() == ExcelAnalysisStopException.class;
    }


    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        log.info("start - invokeHead: {}", JSON.toJSONString(headMap));
        ReadListener.super.invokeHead(headMap, context);
    }

    @Override
    public void invoke(T uploadData, AnalysisContext context) {
        if (Thread.currentThread().isInterrupted()) {
            // 停止invoke
            throw new ExcelAnalysisStopException();
        }
        totalReadNum++;

        // sheetNo rowNo
        ReadSheetHolder readSheetHolder = context.readSheetHolder();
        Integer sheetNo = readSheetHolder.getSheetNo();
        Integer rowNo = getRowNo(readSheetHolder);
        uploadData.setSheetNo(sheetNo);
        uploadData.setRowNo(rowNo);

        if (skipRowPosition(uploadData)) {
            if (!batchContainer.isEmpty()) {
                LinkedList<T> threadBatchList = new LinkedList<>(batchContainer);
                batchContainer.clear();
                // execute
                executeBatch(threadBatchList);
            }
            return;
        }

        if (!worker.check(uploadData)) {
            return;
        }

        batchContainer.add(uploadData);

        if (batchContainer.size() >= config.getBatchSize()) {
            LinkedList<T> threadBatchList = new LinkedList<>(batchContainer);
            batchContainer.clear();

            // execute
            executeBatch(threadBatchList);
        }
    }

    private void executeBatch(LinkedList<T> threadBatchList) {
        // 多线程执行
        executorService.execute(() -> doExecuteBatch(threadBatchList));
    }

    private void doExecuteBatch(LinkedList<T> threadBatchList) {
        int size = threadBatchList.size();
        T first = threadBatchList.getFirst();
        T last = threadBatchList.getLast();
        log.info("doExecuteBatch - threadBatchList [{}~{}]", ExcelUtil.getRowPosition(first), ExcelUtil.getRowPosition(last));
        try {
            worker.doWork(threadBatchList);
            // 记录最大行号
            recordSuccessRowPosition(last, size);
        } catch (Exception e) {
            log.error("doWork error - threadBatchList [{}~{}]", ExcelUtil.getRowPosition(first), ExcelUtil.getRowPosition(last));
            if (size == 1) {
                recordErrorRowPosition(first);
                autoCheckAbnormalScheduler.putAbnormal(ExcelUtil.getRowPosition(first));
                log.error("doWork error - singeData: {}", JSON.toJSONString(first), e);
                return;
            }
            for (T singeData : threadBatchList) {
                try {
                    LinkedList<T> singeList = new LinkedList<>();
                    singeList.add(singeData);
                    worker.doWork(singeList);
                    recordSuccessRowPosition(singeData, 1);
                } catch (Exception ex) {
                    recordErrorRowPosition(singeData);
                    autoCheckAbnormalScheduler.putAbnormal(ExcelUtil.getRowPosition(singeData));
                    log.error("doWork error - singeData: {}", JSON.toJSONString(singeData), ex);
                }
            }
        } finally {
            threadBatchList.clear();
        }
    }

    private void recordSuccessRowPosition(T last, int successSize) {
        this.config.getRefreshStrategy().recordSuccessRowPosition(last);
        successNum.add(successSize);
    }

    private void recordErrorRowPosition(T singeData) {
        this.config.getRefreshStrategy().recordErrorRowPosition(singeData, this::applyFail) ;
    }

    private void applyFail(String message) {
        failNum++;
    }

    private boolean skipRowPosition(T row) {
        String position = ExcelUtil.getRowPosition(row);
        if (skipError(position) || skipIfNotFromToEnd(position) || skipMaxSuccess(position)) {
            return true;
        }
        // 到达成功记录，新行
        log.info("开始读取新行 sheet.row: {}.{}", row.getSheetNo(), row.getRowNo());
        return false;
    }

    private boolean skipMaxSuccess(String position) {
        if (maxSuccessSnapshotPosition == null) {
            // 没有成功记录，从头开始直到结束
            return false;
        }
        // 没有到达成功记录，跳过
        return ExcelUtil.comparePosition(position, maxSuccessSnapshotPosition) <= 0;
    }

    private boolean skipIfNotFromToEnd(String position) {
        return config.getFromSheetRowNo() != null && ExcelUtil.comparePosition(position, config.getFromSheetRowNo()) < 0
                || config.getEndSheetRowNo() != null && ExcelUtil.comparePosition(position, config.getEndSheetRowNo()) > 0;
    }

    private boolean skipError(String position) {
        if (errorMapSnapshot == null || errorMapSnapshot.isEmpty()) {
            return false;
        }
        // 跳过错误记录
        if (errorMapSnapshot.get(position) != null) {
            errorMapSnapshot.remove(position);
            return true;
        }
        return false;
    }

    private Integer getRowNo(ReadSheetHolder readSheetHolder) {
        return readSheetHolder.getRowIndex() + 1;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("start - doAfterAllAnalysed sheetNo: {}", context.readSheetHolder().getSheetNo());
        Integer sheetNo = context.readSheetHolder().getSheetNo();
        List<ReadSheet> readSheets = context.readWorkbookHolder().getParameterSheetDataList();

        // 最后一个sheet
        if (sheetNo == readSheets.size() - 1 && (!batchContainer.isEmpty())) {
            // execute
            executeBatch(batchContainer);
        }
    }

    void shutdown() {
        log.info("finish shutdown...");
        // 停止线程池
        shutdownAwait();
        // 停止刷新
        this.config.getRefreshStrategy().shutdown();
        // 停止检查
        this.autoCheckAbnormalScheduler.shutdown();
    }

    /**
     * 关闭线程池 并 等待执行完成
     */
    private void shutdownAwait() {
        if (this.executorService == null) {
            return;
        }
        // 停止接受新的任务
        this.executorService.shutdown();
        if (Thread.currentThread().isInterrupted()) {
            // 防止awaitTermination被中断
            Thread.interrupted();
        }
        try {
            // 等待所有任务执行完成
            this.executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("shutdownAwait Interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

}
