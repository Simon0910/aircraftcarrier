package com.aircraftcarrier.framework.excel;

import com.aircraftcarrier.framework.excel.util.ExcelRow;
import com.aircraftcarrier.framework.model.BatchResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractImportUpload<T extends ExcelRow> {

    /**
     * batch size
     */
    public final int batchSize;
    /**
     * Result
     */
    public final BatchResult batchResult = new BatchResult();
    /**
     * rows
     */
    public final List<T> rowList;

    /**
     * AbstractImportUpload
     *
     * @param list list
     */
    public AbstractImportUpload(List<T> list) {
        this(list, 1000);
    }


    /**
     * AbstractImportUpload
     *
     * @param list      list
     * @param batchSize batchSize
     */
    public AbstractImportUpload(List<T> list, int batchSize) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("list must not be empty");
        }
        if (batchSize < 10) {
            throw new IllegalArgumentException("batchSize is too small");
        }

        this.rowList = list;
        this.batchSize = batchSize;
    }


    /**
     * do check
     *
     * @param list list
     * @return newList
     */
    List<T> doCheck(List<T> list) {
        List<T> allBatchCheckedList = new ArrayList<>(list.size());
        List<T> tempList = new ArrayList<>(batchSize);
        int i = 1;
        for (Iterator<T> it = list.iterator(); it.hasNext(); i++) {
            // step 1
            T t = preCheck(it.next());
            if (t == null) {
                continue;
            }
            tempList.add(t);
            if (i % batchSize == 0) {
                // step 2
                List<T> checkedList = preBatchCheck(tempList);
                tempList.clear();
                if (!checkedList.isEmpty()) {
                    allBatchCheckedList.addAll(checkedList);
                }
            }
        }

        if (!tempList.isEmpty()) {
            // step 2
            List<T> checkedList = preBatchCheck(tempList);
            tempList.clear(); // help gc
            if (!checkedList.isEmpty()) {
                allBatchCheckedList.addAll(checkedList);
            }
        }

        // step 3
        List<T> allCheckedList = preAllCheck(allBatchCheckedList);
        allBatchCheckedList.clear();
        return allCheckedList;
    }


    /**
     * do invoke
     */
    void doInvoke(List<T> list) {
        List<T> tempList = new ArrayList<>(batchSize);
        int i = 1;
        for (Iterator<T> it = list.iterator(); it.hasNext(); i++) {
            tempList.add(it.next());
            if (i % batchSize == 0) {
                doBatchInvoke(tempList);
                tempList.clear();
            }
        }

        if (!tempList.isEmpty()) {
            doBatchInvoke(tempList);
            tempList.clear(); // help gc
        }
    }


    /**
     * 获取最终上传结果
     *
     * @return BatchResult
     */
    public BatchResult getBatchResult() {
        return batchResult;
    }

    /**
     * filter
     *
     * @param rowList rowList
     * @return newList
     */
    protected abstract List<T> filter(List<T> rowList);

    /**
     * preCheck
     *
     * @param row row
     * @return T
     */
    protected abstract T preCheck(T row);

    /**
     * preBatchCheck
     *
     * @param rowList rowList
     * @return newList
     */
    protected abstract List<T> preBatchCheck(List<T> rowList);

    /**
     * preAllCheck
     *
     * @param rowList rowList
     * @return newList
     */
    protected abstract List<T> preAllCheck(List<T> rowList);


    /**
     * doBatchInvoke
     *
     * @param rowList rowList
     */
    protected abstract void doBatchInvoke(List<T> rowList);
}
