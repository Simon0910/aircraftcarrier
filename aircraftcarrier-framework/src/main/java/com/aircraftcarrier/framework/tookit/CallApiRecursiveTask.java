package com.aircraftcarrier.framework.tookit;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;

/**
 * @author liuzhipeng
 */
public class CallApiRecursiveTask<T, R> extends RecursiveTask<List<R>> {

    /**
     * 切分粒度
     */
    private final int granularity;
    private final List<T> params;
    private final Function<T, R> function;

    public CallApiRecursiveTask(Function<T, R> function, List<T> params) {
        this.function = function;
        this.params = params;
        this.granularity = 1;
    }

    public CallApiRecursiveTask(Function<T, R> function, List<T> params, int granularity) {
        this.function = function;
        this.params = params;
        this.granularity = granularity;
    }

    @Override
    protected List<R> compute() {
        if (params.size() > granularity) {
            return ForkJoinTask.invokeAll(createSubtasks()).stream().map(ForkJoinTask::join).flatMap(Collection::stream).toList();
        } else {
            return processing(params);
        }
    }

    private List<CallApiRecursiveTask<T, R>> createSubtasks() {
        List<List<T>> partition = Lists.partition(params, granularity);
        List<CallApiRecursiveTask<T, R>> callTasks = new ArrayList<>(partition.size());
        for (List<T> partitionParams : partition) {
            callTasks.add(new CallApiRecursiveTask<>(function, partitionParams));
        }
        return callTasks;
    }

    private List<R> processing(List<T> params) {
        List<R> results = new ArrayList<>(params.size());
        for (T param : params) {
            R result = function.apply(param);
            results.add(result);
        }
        return results;
    }
}
