package com.aircraftcarrier.framework.tookit;

import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.aircraftcarrier.framework.concurrent.CallableVoid;
import com.aircraftcarrier.framework.exception.ThreadException;
import com.aircraftcarrier.framework.support.trace.TraceThreadPoolExecutor;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
     * buildThreadFactory
     *
     * @param pooName pooName
     * @return ThreadFactory
     */
    private static ThreadFactory buildThreadFactory(String pooName) {
//        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        return ThreadFactoryBuilder
                .create()
                .setDaemon(false)
                .setNamePrefix(pooName + "-")
                .build();
    }

    /**
     * 默认
     */
    private static ExecutorService commonPool() {
        return new ForkJoinPool(Runtime.getRuntime().availableProcessors(),
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                null, true);
    }

    /**
     * 新增固定线程池 不需要队列（防止无限创建队列任务oom） 多余的请求直接丢弃
     * <p>
     * 使用场景：
     * 1. newFixedThreadPool(1,"xxx-task"); // 开启一个后台监控任务
     */
    public static ExecutorService newFixedThreadPool(int nThreads, String pooName) {
        return new TraceThreadPoolExecutor(
                // 固定大小
                nThreads, nThreads,
                0L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                buildThreadFactory("fix-discard-pool-" + pooName),
                // 忽略其他请求
                new ThreadPoolExecutor.DiscardPolicy());
    }

    /**
     * 新建缓存线程池 固定线程大小(防止无限创建缓存线程oom) 多余的请求直接丢弃
     * <p>
     * 使用场景：
     * 1. newCachedThreadPool(1,"refresh-token"); // 提前1小时，派一个线程取刷新token
     */
    public static ExecutorService newCachedThreadPool(int nThreads, String pooName) {
        return new TraceThreadPoolExecutor(
                // 固定大小
                0, nThreads,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                buildThreadFactory("cached-discard-pool-" + pooName),
                // 忽略其他请求
                new ThreadPoolExecutor.DiscardPolicy());
    }

    /**
     * 新建单线程执行器 1个线程串行执行所有任务 50000队列任务（防止无限创建队列任务oom）多余的请求直接丢弃
     * 使用场景：限流
     */
    public static ExecutorService newSingleThreadExecutor(String pooName) {
//        return Executors.newSingleThreadExecutor();
        return ExecutorBuilder.create()
                .setCorePoolSize(1).setMaxPoolSize(1)
                .setKeepAliveTime(0L, TimeUnit.MILLISECONDS)
                .setWorkQueue(new LinkedBlockingQueue<Runnable>(50000))
                .setThreadFactory(buildThreadFactory("single-discard-pool-" + pooName))
                .setHandler(new ThreadPoolExecutor.DiscardPolicy())
                .buildFinalizable();
    }

    /**
     * newScheduledThreadPool
     */
    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize, String pooName) {
        return Executors.newScheduledThreadPool(corePoolSize);
    }

    /**
     * 新建工作窃取执行器
     */
    public static ExecutorService newWorkStealingPool(String pooName) {
        return Executors.newWorkStealingPool();
    }

    /**
     * newSingleThreadScheduledExecutor
     */
    public static ScheduledExecutorService newSingleThreadScheduledExecutor(String pooName) {
        return Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * newThreadPerTaskExecutor
     */
//    public static ExecutorService newThreadPerTaskExecutor(String pooName) {
//        ThreadFactory threadFactory = ThreadFactoryBuilder
//                .create()
//                .setDaemon(true)
//                .setNameFormat("newThreadPerTask-pool" + pooName + "-%d")
//                .get();
//        return Executors.newThreadPerTaskExecutor(threadFactory);
//    }

    /**
     * newVirtualThreadPerTaskExecutor
     */
//    public static ExecutorService newVirtualThreadPerTaskExecutor(String pooName) {
//        return Executors.newVirtualThreadPerTaskExecutor();
//    }

    /**
     * executeVoid
     */
    public static void invokeVoid(CallableVoid callableVoid) {
        invokeAllVoid(commonPool(), List.of(callableVoid));
    }

    /**
     * executeVoid
     */
    public static void invokeVoid(ExecutorService executor, CallableVoid callableVoid) {
        invokeAllVoid(executor, List.of(callableVoid));
    }

    /**
     * executeAllVoid
     */
    public static void invokeAllVoid(List<CallableVoid> asyncBatchTasks) {
        invokeAllVoid(commonPool(), asyncBatchTasks, false);
    }

    /**
     * executeAllVoid
     */
    public static void invokeAllVoid(ExecutorService executor, List<CallableVoid> asyncBatchTasks) {
        invokeAllVoid(executor, asyncBatchTasks, false);
    }

    /**
     * executeAllVoid ignoreFail
     */
    public static void invokeAllVoid(ExecutorService executor, List<CallableVoid> asyncBatchTasks, boolean ignoreFail) {
        List<Callable<String>> callables = new ArrayList<>(asyncBatchTasks.size());
        for (CallableVoid task : asyncBatchTasks) {
            callables.add(() -> {
                try {
                    task.call();
                } catch (Exception e) {
                    throw new ThreadException(e);
                }
                return "Void";
            });
        }

        invokeAll(executor, callables, ignoreFail);
    }

    /**
     * execute
     */
    public static <T> T invoke(Callable<T> callable) {
        List<T> list = invokeAll(commonPool(), List.of(callable));
        return list.get(0);
    }

    /**
     * execute
     */
    public static <T> T invoke(ExecutorService executor, Callable<T> callable) {
        List<T> list = invokeAll(executor, List.of(callable));
        return list.get(0);
    }

    /**
     * executeAll
     */
    public static <T> List<T> invokeAll(List<Callable<T>> asyncBatchTasks) {
        return invokeAll(commonPool(), asyncBatchTasks, false);
    }

    /**
     * executeAll
     */
    public static <T> List<T> invokeAll(ExecutorService executor, List<Callable<T>> asyncBatchTasks) {
        return invokeAll(executor, asyncBatchTasks, false);
    }

    /**
     * executeAll Ignore Fail
     */
    public static <T> List<T> invokeAll(ExecutorService executor, List<Callable<T>> asyncBatchTasks, boolean ignoreFail) {
        // 异步执行
        List<Future<T>> futures;
        try {
            futures = executor.invokeAll(asyncBatchTasks);
            // 等待批量任务执行完成。。。
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("invokeAll - Interrupted error: ", e);
            throw new ThreadException(e);
        }

        // 按list顺序获取
        List<T> resultList = new ArrayList<>(futures.size());
        for (Future<T> future : futures) {
            try {
                T result = future.get();
//                T result = future.get(10000, TimeUnit.MILLISECONDS);
                resultList.add(result);
                log.debug("get result: {}", JSON.toJSONString(result));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("get - InterruptedException: ", e);
                if (!ignoreFail) {
                    throw new ThreadException(e);
                }
            } catch (ExecutionException e) {
                log.error("get - ExecutionException: ", e);
                if (!ignoreFail) {
                    throw new ThreadException(e);
                }
            }
//            catch (TimeoutException e) {
//                log.error("get - TimeoutException: ", e);
//                if (!ignoreFail) {
//                    throw new ThreadException(e);
//                }
//            }
        }
        return resultList;
    }

    public static Future<?> submit(ExecutorService executor, Runnable task) {
        return executor.submit(task);
    }

    public static <T> Future<T> submit(ExecutorService executor, Callable<T> task) {
        return executor.submit(task);
    }

}
