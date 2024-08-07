package com.aircraftcarrier.framework.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * CallApiParallelTask
 *
 * @param <T>
 * @param <R>
 * @author zhipengliu
 */
public class CallParallelTask<T, R> {

    /**
     * 切分粒度
     */
    private final int granularity;
    private final List<T> params;
    private final Function<T, R> function;

    public CallParallelTask(Function<T, R> function, List<T> params) {
        this.function = function;
        this.params = params;
        this.granularity = 1;
    }

    public CallParallelTask(Function<T, R> function, List<T> params, int granularity) {
        this.function = function;
        this.params = params;
        this.granularity = granularity;
    }

    List<Callable<R>> getTaskList() {
        List<Callable<R>> taskList = new ArrayList<>(params.size());
        for (T param : params) {
            taskList.add(() -> function.apply(param));
        }
        return taskList;
    }

    int getGranularity() {
        return granularity;
    }

}
