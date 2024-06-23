package com.aircraftcarrier.framework.exceltask;

import com.aircraftcarrier.framework.tookit.ApplicationContextUtil;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

/**
 * AbstractTaskWorker
 *
 * @author zhipengliu
 * @date 2023/8/13
 * @since 1.0
 */
public abstract class AbstractTaskWorker<T extends AbstractExcelRow> extends AbstractTask<T> implements Worker<T> {

    private TaskExecutor executor;

    private volatile Integer rowNoByProgressProcessed;

    @Override
    public String start() {
        Class<T> modelClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return getTaskExecutor().start(this, modelClass);
    }

    @Override
    public String stop() {
        return getTaskExecutor().stop(this);
    }

    @Override
    public String reset() {
        return getTaskExecutor().reset(this);
    }

    @Override
    public String resetSuccessSheetRow(String maxSuccessSheetRow) throws IOException {
        return getTaskExecutor().resetSuccessSheetRow(this, maxSuccessSheetRow);
    }

    @Override
    public String settingFromWithEnd(String fromSheetRow, String endSheetRow) {
        return getTaskExecutor().settingFromWithEnd(this, fromSheetRow, endSheetRow);
    }

    protected TaskExecutor getTaskExecutor() {
        if (this.executor == null) {
            this.executor = ApplicationContextUtil.getBean(TaskExecutor.class);
        }
        return executor;
    }

    protected void setRowNoByProgressProcessed(Integer rowNoByProgressProcessed) {
        this.rowNoByProgressProcessed = rowNoByProgressProcessed;
    }

    @Override
    public Integer getRowNoByProgressProcessed() {
        return rowNoByProgressProcessed;
    }
}
