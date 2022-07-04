package com.aircraftcarrier.framework.excel;

import com.aircraftcarrier.framework.excel.util.ExcelRow;
import com.aircraftcarrier.framework.model.BatchResult;
import com.aircraftcarrier.framework.tookit.PredicateUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lzp
 */
@Slf4j
public abstract class AbstractImportUploadWorker<T extends ExcelRow> extends AbstractImportUpload<T> {

    /**
     * ImportUploadWorker
     */
    protected AbstractImportUploadWorker() {
    }

    /**
     * AbstractUploadWorker
     *
     * @param list list
     */
    protected AbstractImportUploadWorker(List<T> list) {
        super(list);
    }

    /**
     * AbstractUploadWorker
     *
     * @param list            list
     * @param batchCheckSize  batchCheckSize
     * @param batchInvokeSize batchInvokeSize
     */
    protected AbstractImportUploadWorker(List<T> list, int batchCheckSize, int batchInvokeSize) {
        super(list, batchCheckSize, batchInvokeSize);
    }

    /**
     * filter
     * 整体去重过滤
     *
     * @param rowList rowList
     * @return List
     */
    @Override
    protected List<T> filter(List<T> rowList) {
        return rowList.stream().filter(PredicateUtil.distinctByKey(T::genUniqueKey, rowList.size())).collect(Collectors.toList());
    }

    /**
     * preCheck
     * 参数基本校验，字段关联校验
     *
     * @param row row
     * @return T
     */
    @Override
    protected T preCheck(T row) {
        return row;
    }

    /**
     * preBatchCheck
     * 接口查询校验，数据库查询校验
     *
     * @param rowList rowList
     * @return List
     */
    @Override
    protected List<T> preBatchCheck(List<T> rowList) {
        log.debug("preBatchCheck - before - size: {}", rowList.size());
        log.debug("preBatchCheck - after  - size: {}", rowList.size());
        return rowList;
    }

    /**
     * preAllCheck
     * 自定义分组校验
     *
     * @param rowList rowList
     * @return List
     */
    @Override
    protected List<T> preAllCheck(List<T> rowList) {
        log.debug("preAllCheck - before - size: {}", rowList.size());
        log.debug("preAllCheck - after  - size: {}", rowList.size());
        return rowList;
    }

    @Override
    protected void doBatchInvoke(List<T> rowList) {
        log.debug("doBatchInvoke - rowList - size: {}", rowList.size());
    }

    /**
     * do work
     */
    public BatchResult doWork() {
        log.debug("doWork start...");
        log.debug("rowList.size: {}", rowList.size());
        // filter
        List<T> filterList = filter(rowList);
        log.debug("filter  - after  - filterList.size: {}", filterList.size());
        // do check
        List<T> checkedList = doCheck(filterList);
        log.debug("doCheck - after  - checkedList.size: {}", checkedList.size());
        // do invoke
        doInvoke(checkedList);
        return batchResult;
    }

    /**
     * builder
     *
     * @return ImportUploadWorkerBuilder
     */
    public ImportUploadWorkerBuilder<T> builder() {
        return new ImportUploadWorkerBuilder<>(this);
    }

    /**
     * ImportUploadWorkerBuilder
     *
     * @param <T>
     */
    public static class ImportUploadWorkerBuilder<T extends ExcelRow> {

        /**
         * worker
         */
        private final AbstractImportUploadWorker<T> worker;

        /**
         * batch check size
         */
        private int batchCheckSize;

        /**
         * batch invoke size
         */
        private int batchInvokeSize;

        /**
         * rows
         */
        private List<T> rowList = new ArrayList<>();

        ImportUploadWorkerBuilder(AbstractImportUploadWorker<T> worker) {
            this.worker = worker;
        }

        public ImportUploadWorkerBuilder<T> rowList(final List<T> rowList) {
            this.rowList = rowList;
            return this;
        }

        public ImportUploadWorkerBuilder<T> batchCheckSize(final int batchCheckSize) {
            this.batchCheckSize = batchCheckSize;
            return this;
        }

        public ImportUploadWorkerBuilder<T> batchInvokeSize(final int batchInvokeSize) {
            this.batchInvokeSize = batchInvokeSize;
            return this;
        }

        public AbstractImportUploadWorker<T> build() {
            worker.batchCheckSize = this.batchCheckSize;
            worker.batchInvokeSize = this.batchInvokeSize;
            worker.rowList = this.rowList;
            return worker;
        }

        @Override
        public String toString() {
            return "ImportUploadWorkerBuilder{" + "rowList.size=" + rowList.size() + "batchCheckSize=" + batchCheckSize + ", batchInvokeSize=" + batchInvokeSize + '}';
        }
    }


}