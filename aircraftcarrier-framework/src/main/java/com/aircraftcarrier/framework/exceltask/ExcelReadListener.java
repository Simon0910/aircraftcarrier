package com.aircraftcarrier.framework.exceltask;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import com.aircraftcarrier.framework.concurrent.ThreadPoolUtil;
import com.aircraftcarrier.framework.concurrent.ThreadUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelAnalysisStopException;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.read.metadata.holder.ReadSheetHolder;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
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

    /**
     * config
     */
    private final TaskConfig config;
    /**
     * 统计处理成功的总数
     */
    private final LongAdder successNum = new LongAdder();
    /**
     * 错误的记录
     * position : 线程号 value : sheetNo.rowNo
     * 每隔3秒记录一下
     */
    private final HashMap<String, String> errorMap = new HashMap<>();
    /**
     * 记录各个线程执行到第几行
     * position : 线程号 value : sheetNo.rowNo
     * 每隔3秒记录一下
     */
    private final HashMap<String, String> successMap;
    /**
     * pool
     */
    private final ThreadPoolExecutor threadPoolExecutor;
    /**
     * 定时刷新 map快照 errorMap快照
     * 任务重启后从最新位置开始
     */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Object errorLock = new Object();
    private final AbstractTaskWorker<T> taskWorker;
    private final int batchSize;
    private final LinkedList<T> batchContainer;
    private final HashMap<String, String> errorMapSnapshot = new HashMap<>();
    /**
     * 强制从第几行开始: sheetNo_rowNo
     */
    private final String fromSheetRowNo;
    /**
     * 从第几行结束: sheetNo_rowNo
     */
    private final String endSheetRowNo;
    private final AutoCheckAbnormalScheduler autoCheckTimer;
    /**
     * totalReadNum
     */
    private int totalReadNum;
    /**
     * 统计失败数
     */
    private int failNum;
    /**
     * 成功的最大行号
     */
    private String maxSuccessSnapshotPosition;
    /**
     * continueToTheEnd
     */
    private boolean continueToTheEnd = false;
    private BufferedWriter errorBufferedWriter;
    // https://cloud.tencent.com/developer/news/783592
    private MappedByteBuffer byteBuffer;

    ExcelReadListener(AbstractTaskWorker<T> taskWorker) throws IOException {
        this.config = taskWorker.config();
        this.batchSize = config.getBatchSize();
        this.batchContainer = new LinkedList<>();
        this.threadPoolExecutor = newFixedThreadPoolWithSyncBockedPolicy(config.getThreadNum(), config.getPoolName());
        this.taskWorker = taskWorker;
        this.successMap = Maps.newHashMapWithExpectedSize(config.getThreadNum());
        this.fromSheetRowNo = config.getFromSheetRowNo();
        this.endSheetRowNo = config.getEndSheetRowNo();
        this.autoCheckTimer = new AutoCheckAbnormalScheduler(taskWorker);
        loadSnapshot();
        startRefreshSnapshot();
        startAutoCheckTimer();
    }

    private void startAutoCheckTimer() {
        // 是否启用 config
        if (config.isEnableAbnormalAutoCheck()) {
            autoCheckTimer.start();
        }
    }

    private void loadSnapshot() {
        try {
            // check
            config.preCheckFile();
            loadErrorMapSnapshot();
            loadSuccessMapSnapshot();
        } catch (Exception e) {
            throw new ExcelTaskException("loadSuccessMapSnapshot io error", e);
        }

        log.info("init - maxSuccessSnapshotPosition {}", maxSuccessSnapshotPosition);
    }

    private void loadSuccessMapSnapshot() throws IOException {
        String successStr = readFromFilePath(config.getSuccessMapSnapshotFilePath());
        String max = "0_0";
        if (CharSequenceUtil.isNotBlank(successStr)) {
            successStr = successStr.trim();
            // 2_10,2_11,$ 写入 1_1000,1_1001,$ ===> 2_10,2_11,$01,$
            successStr = successStr.substring(0, successStr.indexOf(TaskConfig.END));
            for (String next : Splitter.on(StrPool.COMMA).omitEmptyStrings().trimResults().split(successStr)) {
                if (comparePosition(max, next) < 0) {
                    max = maxSuccessSnapshotPosition = next;
                }
            }
        }
    }

    private void loadErrorMapSnapshot() throws IOException {
        String errorStr = readFromFilePath(config.getErrorMapSnapshotFilePath());
        if (CharSequenceUtil.isNotBlank(errorStr)) {
            for (String next : Splitter.on(StrPool.COMMA).omitEmptyStrings().trimResults().split(errorStr)) {
                errorMapSnapshot.put(next, CharSequenceUtil.EMPTY);
            }
        }
    }

    private int comparePosition(String s1, String s2) {
        String[] split1 = s1.split(StrPool.UNDERLINE);
        String[] split2 = s2.split(StrPool.UNDERLINE);
        int result = Integer.parseInt(split1[0]) - Integer.parseInt(split2[0]);
        if (result == 0) {
            result = Integer.parseInt(split1[1]) - Integer.parseInt(split2[1]);
        }
        return result;
    }

    private String readFromFilePath(String filePath) throws IOException {
        // read
        StringBuilder builder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private void startRefreshSnapshot() throws IOException {
        errorBufferedWriter = new BufferedWriter(new FileWriter(config.getErrorMapSnapshotFilePath(), true));
        // 单个sheet 行数最大1048576（7个占位符） 列数最大16384
        // 7位 + 一个逗号 = 8位空
        int placeholderNum = 8;
        try (RandomAccessFile successRandomAccessFile = new RandomAccessFile(config.getSuccessMapSnapshotFilePath(), "rw")) {
            FileChannel fileChannel = successRandomAccessFile.getChannel();
            byteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, (long) config.getThreadNum() * placeholderNum + TaskConfig.END.length());
        }
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // 刷新错误记录
                doRefreshErrorMapSnapshot();
                // 刷新最新快照
                doRefreshSuccessMapSnapshot();
            } catch (Exception e) {
                log.error("init - refreshMapSnapshot error: {}", e.getMessage(), e);
            }
        }, 0, config.getRefreshSnapshotPeriod(), TimeUnit.MILLISECONDS);
    }

    /**
     * newFixedThreadPoolWithSyncBockedHandler
     *
     * @param nThreads nThreads
     * @param poolName poolName
     * @return ThreadPoolExecutor
     * @see Executors#newCachedThreadPool(ThreadFactory)
     */
    private ThreadPoolExecutor newFixedThreadPoolWithSyncBockedPolicy(int nThreads, String poolName) {
        return new ThreadPoolExecutor(
                // 指定大小
                nThreads, nThreads,
                // keepAliveTime
                30, TimeUnit.SECONDS,
                // SynchronousQueue
                new SynchronousQueue<>(),
                // factory
                ThreadPoolUtil.newThreadFactory(poolName),
                //  Block Policy
                ThreadPoolUtil.newBlockPolicy());
    }

    private Integer getRowNo(ReadSheetHolder readSheetHolder) {
        return readSheetHolder.getRowIndex() + 1;
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
        return e.getClass() == RejectedExecutionException.class || e.getClass() == ExcelAnalysisStopException.class;
    }

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        log.info("start - invokeHead: {}", JSON.toJSONString(headMap));
        ReadListener.super.invokeHead(headMap, context);
    }

    @Override
    public void invoke(T uploadData, AnalysisContext context) {
        totalReadNum++;
        if (Thread.currentThread().isInterrupted()) {
            // 停止invoke
            throw new ExcelAnalysisStopException();
        }

        // sheetNo rowNo
        ReadSheetHolder readSheetHolder = context.readSheetHolder();
        Integer sheetNo = readSheetHolder.getSheetNo();
        Integer rowNo = getRowNo(readSheetHolder);
        uploadData.setSheetNo(sheetNo);
        uploadData.setRowNo(rowNo);

        if (skipRowPosition(uploadData)) {
            return;
        }

        if (!taskWorker.check(uploadData)) {
            return;
        }

        batchContainer.add(uploadData);

        if (batchContainer.size() >= batchSize) {
            LinkedList<T> threadBatchList = new LinkedList<>(batchContainer);
            batchContainer.clear();

            // execute
            executeBatch(threadBatchList);
        }
    }

    private boolean skipRowPosition(T row) {
        String position = getRowPosition(row);
        // 跳过错误记录
        if (!errorMapSnapshot.isEmpty() && errorMapSnapshot.get(position) != null) {
            errorMapSnapshot.remove(position);
            return true;
        }

        if (continueToTheEnd) {
            return false;
        }

        // fromSheetRowNo 高优先级
        if (fromSheetRowNo != null) {
            // 开始行
            if (comparePosition(position, fromSheetRowNo) < 0) {
                return true;
            }
            if (endSheetRowNo == null) {
                // 到达开始行
                log.info("开始读取新行 sheet.row: {}.{}", row.getSheetNo(), row.getRowNo());
                continueToTheEnd = true;
                return false;
            }
            // 没有到结束行
            if (comparePosition(position, endSheetRowNo) <= 0) {
                return false;
            }
            // 到达结束行, 停止读取
            if (!batchContainer.isEmpty()) {
                executeBatch(batchContainer);
            }
            try {
                ThreadUtil.sleepSeconds(1);
            } catch (InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }
            throw new ExcelAnalysisStopException();
        }

        // maxSuccessSnapshotPosition 低优先级
        if (maxSuccessSnapshotPosition == null) {
            // 没有成功记录，从头开始直到结束
            continueToTheEnd = true;
            return false;
        }

        if (comparePosition(position, maxSuccessSnapshotPosition) <= 0) {
            // 没有到达成功记录，跳过
            return true;
        }

        // 到达成功记录，新行
        log.info("开始读取新行 sheet.row: {}.{}", row.getSheetNo(), row.getRowNo());
        continueToTheEnd = true;
        return false;
    }

    private void recordSuccessRowPosition(T row, int successSize) {
        successMap.put(ThreadUtil.getThreadNo(), getRowPosition(row));
        successNum.add(successSize);
    }

    private void recordErrorRowPosition(T row) {
        synchronized (errorLock) {
            // 不存在增加
            errorMap.computeIfAbsent(getRowPosition(row), k -> {
                failNum++;
                autoCheckTimer.putAbnormal(getRowPosition(row));
                return CharSequenceUtil.EMPTY;
            });
        }
    }

    private String getRowPosition(T t) {
        return t.getSheetNo() + StrPool.UNDERLINE + t.getRowNo();
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

    private void executeBatch(LinkedList<T> threadBatchList) {
        // 多线程执行
        threadPoolExecutor.execute(() -> doExecuteBatch(threadBatchList));
    }

    private void doExecuteBatch(LinkedList<T> threadBatchList) {
        int size = threadBatchList.size();
        T first = threadBatchList.getFirst();
        T last = threadBatchList.getLast();
        log.info("doExecuteBatch - threadBatchList [{}~{}]", getRowPosition(first), getRowPosition(last));
        try {
            taskWorker.doWork(threadBatchList);
            // 记录最大行号
            recordSuccessRowPosition(last, size);
        } catch (Exception e) {
            log.error("doWork error - threadBatchList [{}~{}]", getRowPosition(first), getRowPosition(last));
            if (size == 1) {
                recordErrorRowPosition(first);
                log.error("doWork error - singeData: {}", JSON.toJSONString(first), e);
                return;
            }
            for (T singeData : threadBatchList) {
                try {
                    LinkedList<T> singeList = new LinkedList<>();
                    singeList.add(singeData);
                    taskWorker.doWork(singeList);
                    recordSuccessRowPosition(singeData, 1);
                } catch (Exception ex) {
                    recordErrorRowPosition(singeData);
                    log.error("doWork error - singeData: {}", JSON.toJSONString(singeData), ex);
                }
            }
        } finally {
            threadBatchList.clear();
        }
    }

    void shutdown() {
        log.info("finish shutdown...");
        try {
            // 停止线程池
            shutdownAwait(threadPoolExecutor);
            // 停止刷新快照任务
            scheduler.shutdownNow();
            // 停止前刷新错误记录
            doRefreshErrorMapSnapshot();
            // 停止前刷新最新快照
            doRefreshSuccessMapSnapshot();
            log.info("finish shutdown - doRefresh totalReadNum: {} - successNum: {}, - failNum: {} = other: {}", totalReadNum, successNum, failNum, totalReadNum - successNum.intValue() - failNum);
        } finally {
            if (byteBuffer != null) {
                byteBuffer.force();
                byteBuffer.clear();
                byteBuffer = null;
            }
            if (errorBufferedWriter != null) {
                try {
                    errorBufferedWriter.close();
                } catch (IOException ex) {
                    errorBufferedWriter = null;
                    log.error("close errorBufferedWriter error ", ex);
                }
            }
            autoCheckTimer.stop();
        }
    }

    private void doRefreshErrorMapSnapshot() {
        if (errorMap.isEmpty()) {
            return;
        }
        synchronized (errorLock) {
            if (errorMap.isEmpty()) {
                return;
            }
            try {
                StringBuilder builder = new StringBuilder();
                for (String sheetRow : errorMap.keySet()) {
                    builder.append(sheetRow).append(StrPool.COMMA);
                }
                builder.append(StrPool.CRLF);
                errorBufferedWriter.write(builder.toString());
                errorBufferedWriter.flush();
            } catch (IOException e) {
                throw new ExcelTaskException("doRefreshErrorMapSnapshot error", e);
            } finally {
                log.info("doRefresh errorMap size {}", errorMap.size());
                errorMap.clear();
            }
        }
    }

    private void doRefreshSuccessMapSnapshot() {
        if (successMap.isEmpty()) {
            log.debug("doRefresh successMap is Empty");
            return;
        }
        try {
            byteBuffer.position(0);
            StringBuilder builder = new StringBuilder();
            for (String sheetRow : successMap.values()) {
                builder.append(sheetRow).append(StrPool.COMMA);
            }
            builder.append(TaskConfig.END);
            byteBuffer.put(builder.toString().getBytes());
        } catch (Exception e) {
            throw new ExcelTaskException("doRefreshSuccessMapSnapshot error", e);
        } finally {
            log.info("doRefresh successNum {}", successNum);
        }
    }

    /**
     * 关闭线程池 并 等待执行完成
     *
     * @param executor executor
     */
    private void shutdownAwait(ThreadPoolExecutor executor) {
        if (executor == null) {
            return;
        }
        try {
            // 停止接受新的任务
            executor.shutdown();
            // 等待所有任务执行完成
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException ignore) {
            // 清除中断标识，使线程池任务执行完毕
            Thread.interrupted();
            try {
                // 停止接受新的任务
                executor.shutdown();
                // 等待所有任务执行完成
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                executor.shutdownNow();
                log.error("shutdownAwait msg: {}", ex.getMessage(), ex);
            } finally {
                Thread.currentThread().interrupt();
            }
        }
    }

}
