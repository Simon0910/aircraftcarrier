package com.aircraftcarrier.framework.excel;

import com.aircraftcarrier.framework.excel.util.ExcelRow;
import com.aircraftcarrier.framework.model.BatchResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author lzp
 */
public abstract class AbstractImportUpload<T extends ExcelRow> {

    /**
     * BATCH_CHECK_SIZE_LOWER
     */
    public static final int BATCH_CHECK_SIZE_LOWER = 10;

    /**
     * BATCH_INVOKE_SIZE_LOWER
     */
    public static final int BATCH_INVOKE_SIZE_LOWER = 10;

    /**
     * Result
     */
    public final BatchResult batchResult = new BatchResult();

    /**
     * batch check size
     */
    protected int batchCheckSize;

    /**
     * batch invoke size
     */
    protected int batchInvokeSize;

    /**
     * rows
     */
    protected List<T> rowList;

    /**
     * AbstractImportUpload
     */
    protected AbstractImportUpload() {
        this(new ArrayList<>());
    }


    /**
     * AbstractImportUpload
     *
     * @param list list
     */
    protected AbstractImportUpload(List<T> list) {
        this(list, 100, 1000);
    }


    /**
     * AbstractImportUpload
     *
     * @param list            list
     * @param batchCheckSize  batchCheckSize
     * @param batchInvokeSize batchInvokeSize
     */
    protected AbstractImportUpload(List<T> list, int batchCheckSize, int batchInvokeSize) {
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
        int i = 1;
        for (Iterator<T> it = list.iterator(); it.hasNext(); i++) {
            // step 1
            T t = preCheck(it.next());
            if (t == null) {
                continue;
            }
            tempList.add(t);
            if (i % batchCheckSize == 0) {
                // step 2
                List<T> checkedList = preBatchCheck(tempList);
                if (!checkedList.isEmpty()) {
                    allBatchCheckedList.addAll(checkedList);
                }
                tempList = new ArrayList<>(batchCheckSize);
            }
        }

        if (!tempList.isEmpty()) {
            // step 2
            List<T> checkedList = preBatchCheck(tempList);
            if (!checkedList.isEmpty()) {
                allBatchCheckedList.addAll(checkedList);
            }
        }

        // step 3
        return preAllCheck(allBatchCheckedList);
    }


    /**
     * do invoke
     */
    void doInvoke(List<T> list) {
        List<T> tempList = new ArrayList<>(batchInvokeSize);
        int i = 1;
        for (Iterator<T> it = list.iterator(); it.hasNext(); i++) {
            tempList.add(it.next());
            if (i % batchInvokeSize == 0) {
                doBatchInvoke(tempList);
                tempList = new ArrayList<>(batchInvokeSize);
            }
        }

        if (!tempList.isEmpty()) {
            doBatchInvoke(tempList);
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
}
