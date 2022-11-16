package com.aircraftcarrier.framework.excel;

import com.aircraftcarrier.framework.excel.util.ExcelRow;
import com.aircraftcarrier.framework.model.BatchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lzp
 */
public abstract class AbstractImportUpload<T extends ExcelRow> {

    /**
     * BATCH_CHECK_SIZE_LOWER
     */
    protected static final int BATCH_CHECK_SIZE_LOWER = 10;

    /**
     * BATCH_INVOKE_SIZE_LOWER
     */
    protected static final int BATCH_INVOKE_SIZE_LOWER = 10;

    /**
     * Result
     */
    protected final BatchResult batchResult = new BatchResult();

    /**
     * batch check size
     */
    int batchCheckSize;

    /**
     * batch invoke size
     */
    int batchInvokeSize;

    /**
     * rows
     */
    List<T> rowList;

    /**
     * AbstractImportUpload
     */
    AbstractImportUpload() {
        this(new ArrayList<>());
    }


    /**
     * AbstractImportUpload
     *
     * @param list list
     */
    AbstractImportUpload(List<T> list) {
        this(list, 100, 1000);
    }


    /**
     * AbstractImportUpload
     *
     * @param list            list
     * @param batchCheckSize  batchCheckSize
     * @param batchInvokeSize batchInvokeSize
     */
    AbstractImportUpload(List<T> list, int batchCheckSize, int batchInvokeSize) {
        if (list == null) {
            throw new IllegalArgumentException("list must not be null");
        }
        if (batchCheckSize < BATCH_CHECK_SIZE_LOWER) {
            throw new IllegalArgumentException("batchCheckSize is too small");
        }
        if (batchInvokeSize < BATCH_INVOKE_SIZE_LOWER) {
            throw new IllegalArgumentException("batchInvokeSize is too small");
        }

        this.rowList = list;
        this.batchCheckSize = batchCheckSize;
        this.batchInvokeSize = batchInvokeSize;
    }


    /**
     * do check
     *
     * @param list list
     * @return newList
     */
    List<T> doCheck(List<T> list) {
        List<T> allBatchCheckedList = new ArrayList<>(list.size());
        List<T> tempList = new ArrayList<>(batchCheckSize);
        for (T value : list) {
            // step 1
            T t = preCheck(value);
            if (t == null) {
                continue;
            }
            tempList.add(t);
            if (batchCheckSize == tempList.size()) {
                // step 2
                List<T> checkedList = preBatchCheck(tempList);
                if (!checkedList.isEmpty()) {
                    allBatchCheckedList.addAll(checkedList);
                    checkedList.clear();
                }
                tempList.clear();
            }
        }

        if (!tempList.isEmpty()) {
            // step 2
            List<T> checkedList = preBatchCheck(tempList);
            if (!checkedList.isEmpty()) {
                allBatchCheckedList.addAll(checkedList);
                checkedList.clear();
            }
            tempList.clear();
        }

        // step 3
        return preAllCheck(allBatchCheckedList);
    }


    /**
     * do invoke
     */
    void doInvoke(List<T> list) {
        List<T> tempList = new ArrayList<>(batchInvokeSize);
        for (T t : list) {
            tempList.add(t);
            if (batchInvokeSize == tempList.size()) {
                doBatchInvoke(tempList);
                tempList.clear();
            }
        }

        if (!tempList.isEmpty()) {
            doBatchInvoke(tempList);
            tempList.clear();
        }
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

    /**
     * afterProcess
     *
     * @param rowList rowList
     */
    protected abstract void afterProcess(List<T> rowList);
}
