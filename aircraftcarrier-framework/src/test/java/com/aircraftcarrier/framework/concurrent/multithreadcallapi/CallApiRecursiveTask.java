package com.aircraftcarrier.framework.concurrent.multithreadcallapi;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        this(function, params, ForkJoinPool.commonPool().getParallelism());
    }

    public CallApiRecursiveTask(Function<T, R> function, List<T> params, int granularity) {
        this.function = function;
        this.params = params;
        this.granularity = granularity;
    }

    @Override
    protected List<R> compute() {
        if (params.size() > granularity) {
            List<List<R>> partitionResults = ForkJoinTask.invokeAll(createSubtasks()).stream().map(ForkJoinTask::join).collect(Collectors.toList());
            Stream<R> headStream = Stream.empty();
            for (List<R> partitionResult : partitionResults) {
                headStream = Stream.concat(headStream, partitionResult.stream());
            }
            return headStream.collect(Collectors.toList());
        } else {
            return processing(params);
        }
    }

    private List<CallApiRecursiveTask<T, R>> createSubtasks() {
        List<CallApiRecursiveTask<T, R>> callTasks = new ArrayList<>(granularity);
        List<List<T>> partition = Lists.partition(params, granularity);
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
