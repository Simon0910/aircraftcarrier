package com.aircraftcarrier.framework.tookit;

import com.aircraftcarrier.framework.concurrent.CallableVoid;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 并发执行工具
 *
 * @author zhipengliu
 * @date 2022/8/19
 * @since 1.0
 */
@Slf4j
public class ThreadPoolUtil {

    /**
     * cpu
     */
    public static final int CORE_SIZE = Runtime.getRuntime().availableProcessors();

    /**
     * 核心线程数
     */
    public static final int CORE_POOL_SIZE = CORE_SIZE * 2;

    /**
     * 线程池最大线程数
     */
    public static final int MAX_POOL_SIZE = CORE_SIZE * 4;

    /**
     * 线程空闲时间
     */
    public static final int KEEP_ALIVE_TIME = 60;


    /**
     * 私有
     */
    private ThreadPoolUtil() {
    }

    /**
     * executeAllVoid
     */
    public static void executeAllVoid(ThreadPoolExecutor pool, List<CallableVoid> batchTasks) {
        List<Callable<String>> callables = new ArrayList<>(batchTasks.size());
        for (CallableVoid batchTask : batchTasks) {
            callables.add(() -> {
                try {
                    batchTask.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return "void";
            });
        }
        executeAll(pool, callables);
    }

    /**
     * executeAll
     */
    public static <T> List<T> executeAll(ThreadPoolExecutor pool, List<Callable<T>> batchTasks) {
        // 异步执行
        List<Future<T>> futures = new ArrayList<>(batchTasks.size());
        try {
            futures = pool.invokeAll(batchTasks);
            // 等待批量任务执行完成。。。
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("invokeAll - Interrupted error: ", e);
        }

        // 按list顺序获取
        List<T> resultList = new ArrayList<>(futures.size());
        for (Future<T> future : futures) {
            try {
                T result = future.get();
                resultList.add(result);
                log.info("get result: {}", JSON.toJSONString(result));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("get - Interrupted error: ", e);
            } catch (ExecutionException e) {
                log.error("get - Execution error: ", e);
                throw new RuntimeException(e);
            }
        }
        return resultList;
    }


}
