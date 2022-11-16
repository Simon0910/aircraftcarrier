package com.aircraftcarrier.framework.excel;

import com.aircraftcarrier.framework.core.Builder;
import com.aircraftcarrier.framework.excel.util.ExcelRow;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lzp
 */
public class ImportUploadWorkerBuilder<T extends ExcelRow> implements Builder<AbstractImportUploadWorker<T>> {

    /**
     * BATCH_CHECK_SIZE_LOWER
     */
    protected static final int BATCH_CHECK_SIZE_LOWER = 10;

    /**
     * BATCH_INVOKE_SIZE_LOWER
     */
    protected static final int BATCH_INVOKE_SIZE_LOWER = 10;

    /**
     * worker
     */
    protected AbstractImportUploadWorker<T> worker;

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

    /**
     * 需要告诉静态一个泛型
     *
     * <pre>{@code
     *  ImportUploadWorkerBuilder.<DemoImportExcel>builder()
     * }</pre>
     */
    public static <T extends ExcelRow> ImportUploadWorkerBuilder<T> builder() {
        return new ImportUploadWorkerBuilder<>();
    }

    /**
     * <pre>{@code
     *   BatchResult batchResult = ImportUploadWorkerBuilder.<DemoImportExcel>builder()
     *      .worker(new ProductDetailsImportUpload())
     *      .rowList(readResult.getRowList())
     *      .batchCheckSize(100)
     *      .batchInvokeSize(1000)
     *      .build().doWork();
     * }</pre>
     */
    public ImportUploadWorkerBuilder<T> worker(final AbstractImportUploadWorker<T> worker) {
        this.worker = worker;
        return this;
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

    @Override
    public AbstractImportUploadWorker<T> build() {
        return build(this);
    }

    private AbstractImportUploadWorker<T> build(ImportUploadWorkerBuilder<T> builder) {
        if (builder.worker == null) {
            throw new IllegalArgumentException("worker must not be null");
        }
        if (builder.rowList == null) {
            throw new IllegalArgumentException("list must not be null");
        }
        if (builder.batchCheckSize < BATCH_CHECK_SIZE_LOWER) {
            throw new IllegalArgumentException("batchCheckSize is too small");
        }
        if (builder.batchInvokeSize < BATCH_INVOKE_SIZE_LOWER) {
            throw new IllegalArgumentException("batchInvokeSize is too small");
        }
        builder.worker.batchCheckSize = builder.batchCheckSize;
        builder.worker.batchInvokeSize = builder.batchInvokeSize;
        builder.worker.rowList = builder.rowList;
        return builder.worker;
    }
}
