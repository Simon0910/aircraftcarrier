package com.aircraftcarrier.framework.tookit;

import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.aircraftcarrier.framework.concurrent.CallableVoid;
import com.aircraftcarrier.framework.concurrent.MyDiscardPolicyRejectedExecutionHandler;
import com.aircraftcarrier.framework.exception.ThreadException;
import com.aircraftcarrier.framework.support.trace.TraceThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
        return ThreadFactoryBuilder
                .create()
                .setDaemon(false)
                .setNamePrefix(pooName + "-")
                .build();
    }

    /**
     * DiscardPolicy
     */
    private static MyDiscardPolicyRejectedExecutionHandler buildDiscardPolicy() {
        return new MyDiscardPolicyRejectedExecutionHandler();
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
                buildDiscardPolicy());
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
                buildDiscardPolicy());
    }

    /**
     * 新建单线程执行器 1个线程串行执行所有任务 50000队列任务（防止无限创建队列任务oom）多余的请求直接丢弃
     * 使用场景：限流
     */
    public static ExecutorService newSingleThreadExecutor(String pooName) {
        return ExecutorBuilder.create()
                .setCorePoolSize(1).setMaxPoolSize(1)
                .setKeepAliveTime(0L, TimeUnit.MILLISECONDS)
                .setWorkQueue(new LinkedBlockingQueue<Runnable>(50000))
                .setThreadFactory(buildThreadFactory("single-discard-pool-" + pooName))
                .setHandler(buildDiscardPolicy())
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
        List<Callable<Void>> callables = new ArrayList<>(asyncBatchTasks.size());
        for (CallableVoid task : asyncBatchTasks) {
            callables.add(() -> {
                task.call();
                return null;
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
    public static <T> List<T> invokeAll(ExecutorService executor, List<Callable<T>> tasks, boolean ignoreFail) {
        if (tasks == null) {
            throw new NullPointerException();
        }
        ArrayList<Future<T>> futures = new ArrayList<>(tasks.size());
        try {
            for (Callable<T> t : tasks) {
                RunnableFuture<T> f = new FutureTask<>(t);
                futures.add(f);
                executor.execute(f);
            }
            List<T> resultList = new ArrayList<>(futures.size());
            for (Future<T> f : futures) {
                if (!f.isDone()) {
                    try {
                        T result = f.get(10, TimeUnit.SECONDS);
                        resultList.add(result);
                    } catch (CancellationException | ExecutionException | InterruptedException | TimeoutException e) {
                        if (!ignoreFail) {
                            throw new ThreadException(e);
                        }
                    }
                }
            }
            return resultList;
        } catch (Throwable t) {
            for (Future<T> future : futures) {
                future.cancel(true);
            }
            throw t;
        }

    }

    public static Future<?> submit(ExecutorService executor, Runnable task) {
        return executor.submit(task);
    }

    public static <T> Future<T> submit(ExecutorService executor, Callable<T> task) {
        return executor.submit(task);
    }

}
