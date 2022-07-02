package com.aircraftcarrier.framework.excel;

import com.aircraftcarrier.framework.excel.util.ExcelRow;
import com.aircraftcarrier.framework.tookit.PredicateUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractImportUploadWorker<T extends ExcelRow> extends AbstractImportUpload<T> {
    /**
     * AbstractUploadWorker
     *
     * @param list list
     */
    public AbstractImportUploadWorker(List<T> list) {
        super(list);
    }

    /**
     * AbstractUploadWorker
     *
     * @param list      list
     * @param batchSize batchSize
     */
    public AbstractImportUploadWorker(List<T> list, int batchSize) {
        super(list, batchSize);
    }

    /**
     * filter
     * 整体去重过滤
     *
     * @param rowList rowList
     * @return
     */
    @Override
    protected List<T> filter(List<T> rowList) {
        return rowList.stream().filter(PredicateUtil.distinctByKey(T::genUniqueKey)).collect(Collectors.toList());
    }

    /**
     * preCheck
     * 参数基本校验，字段关联校验
     *
     * @param row row
     * @return
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
     * @return
     */
    @Override
    protected List<T> preBatchCheck(List<T> rowList) {
        log.debug("preBatchCheck - before - size: {}", rowList.size());
        log.debug("preBatchCheck - after  - size: {}", rowList.size());
        return new ArrayList<>(rowList);
    }

    /**
     * preAllCheck
     * 自定义分组校验
     *
     * @param rowList rowList
     * @return
     */
    @Override
    protected List<T> preAllCheck(List<T> rowList) {
        log.debug("preAllCheck - before - size: {}", rowList.size());
        log.debug("preAllCheck - after  - size: {}", rowList.size());
        return new ArrayList<>(rowList);
    }

    /**
     * do work
     */
    public void doWork() {
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
    }
}
