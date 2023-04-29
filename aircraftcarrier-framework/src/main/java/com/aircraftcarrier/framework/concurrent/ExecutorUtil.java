package com.aircraftcarrier.framework.concurrent;

import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * ExecutorUtil
 *
 * @author zhipengliu
 * @date 2022/8/19
 * @since 1.0
 */
@Slf4j
public class ExecutorUtil {

    /**
     * 私有
     */
    private ExecutorUtil() {
    }


    /**
     * buildThreadFactory
     * <p>
     * {@link ThreadFactoryBuilder#create()
     * .setDaemon(false)
     * .setNamePrefix(pooName + suffix)
     * .build();}
     *
     * @param pooName pooName
     * @return ThreadFactory
     */
    private static ThreadFactory buildThreadFactory(String pooName, String suffix) {
        return ThreadPoolUtil.newThreadFactory(pooName + suffix);
    }


    /**
     * 固定线程池 (队列容量 + block) // 防止队列容量OOM，防止丢弃任务
     *
     * <p>
     * 参考：
     * {@link java.util.concurrent.Executors#newFixedThreadPool(int)} }
     * <p>
     * 使用场景：
     * 1. newFixedThreadPoolBlock(10,"xxxWork"); // 多个线程执行，不丢弃任务
     */
    public static ExecutorService newFixedThreadPoolBlock(int nThreads, String pooName) {
        return newFixedThreadPool(
                nThreads,
                "block-" + pooName,
                new LinkedBlockingQueue<>(1024),
                ThreadPoolUtil.newBlockPolicy());
    }


    /**
     * 固定线程池 (同步队列 + discard) // 防止重复执行任务
     * <p>
     * 参考：
     * {@link java.util.concurrent.Executors#newFixedThreadPool(int)} }
     * <p>
     * 使用场景：
     * 1. newFixedThreadPoolDiscard(1,"xxxTask"); // 开启一个后台监控任务
     */
    public static ExecutorService newFixedThreadPoolDiscard(int nThreads, String pooName) {
        return newFixedThreadPool(
                nThreads,
                "discard-" + pooName,
                new SynchronousQueue<>(),
                ThreadPoolUtil.newDiscardPolicyPlus());
    }


    private static ExecutorService newFixedThreadPool(int nThreads,
                                                      String pooName,
                                                      BlockingQueue<Runnable> blockingQueue,
                                                      RejectedExecutionHandler reject) {
        return new TraceThreadPoolExecutor(
                // 固定大小
                nThreads, nThreads,
                0L, TimeUnit.SECONDS,
                blockingQueue,
                buildThreadFactory("fix-", pooName),
                reject);
    }


    /**
     * 缓存线程池 （同步队列 + Block）// 防止无限创建线程，防止丢弃任务
     */
    public static ExecutorService newCachedThreadPoolBlock(int maxThreads, String pooName) {
        return newCachedThreadPool(
                "block-" + pooName,
                maxThreads,
                ThreadPoolUtil.newBlockPolicy());
    }


    /**
     * 缓存线程池 // 防止无限创建线程，防止丢弃任务
     * <p>
     * 使用场景：
     * 1. newCachedThreadPool(1, "refresh-token"); // 提前1小时，派一个线程取刷新token
     */
    public static ExecutorService newCachedThreadPoolDiscard(int maxThreads, String pooName) {
        return newCachedThreadPool(
                "block-" + pooName,
                maxThreads,
                ThreadPoolUtil.newDiscardPolicyPlus());
    }


    private static ExecutorService newCachedThreadPool(String pooName,
                                                       int maxThreads,
                                                       RejectedExecutionHandler reject) {
        return new TraceThreadPoolExecutor(
                // 指定大小
                0, maxThreads,
                10, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                buildThreadFactory("cached-", pooName),
                reject);
    }


    /**
     * 单线程执行器 1个线程串行执行所有任务 默认上限50000缓冲任务（防止无限创建队列任务oom）多余的请求同步阻塞 （不丢弃任务）
     * 参考：
     * {@link java.util.concurrent.Executors#newSingleThreadExecutor() }
     */
    public static ExecutorService newSingleThreadExecutorBlock(String pooName) {
        return ExecutorBuilder.create()
                .setCorePoolSize(1).setMaxPoolSize(1)
                .setKeepAliveTime(0L, TimeUnit.MILLISECONDS)
                .setWorkQueue(new LinkedBlockingQueue<>(1024))
                .setThreadFactory(buildThreadFactory(pooName, "-single-block"))
                .setHandler(ThreadPoolUtil.newBlockPolicy())
                .buildFinalizable();
    }

    /**
     * 单线程执行器 1个线程串行执行所有任务 默认上限50000缓冲任务（防止无限创建队列任务oom）多余的请求同步阻塞 （不丢弃任务）
     * 参考：
     * {@link java.util.concurrent.Executors#newSingleThreadExecutor() }
     */
    public static ExecutorService newSingleThreadExecutorCaller(String pooName) {
        return ExecutorBuilder.create()
                .setCorePoolSize(1).setMaxPoolSize(1)
                .setKeepAliveTime(0L, TimeUnit.MILLISECONDS)
                .setWorkQueue(new LinkedBlockingQueue<>(1024))
                .setThreadFactory(buildThreadFactory(pooName, "-single-caller"))
                .setHandler(ThreadPoolUtil.newCallerRunsPolicy())
                .buildFinalizable();
    }

    /**
     * 单线程执行器 1个线程串行执行所有任务 同步队列 多余的请求直接丢弃
     * 使用场景：限流
     */
    public static ExecutorService newSingleThreadExecutorDiscard(String pooName) {
        return ExecutorBuilder.create()
                .setCorePoolSize(1).setMaxPoolSize(1)
                .setKeepAliveTime(0L, TimeUnit.MILLISECONDS)
                .setWorkQueue(new SynchronousQueue<>())
                .setThreadFactory(buildThreadFactory(pooName, "-single-discard"))
                .setHandler(ThreadPoolUtil.newDiscardPolicyPlus())
                .buildFinalizable();
    }

    /**
     * newScheduledThreadPool
     * 参考：
     * {@link java.util.concurrent.Executors#newScheduledThreadPool(int)} }
     */
    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize, String pooName) {
        return Executors.newScheduledThreadPool(corePoolSize);
    }

    /**
     * 工作窃取执行器
     * 参考：
     * {@link java.util.concurrent.Executors#newWorkStealingPool(int)} }
     */
    public static ForkJoinPool newWorkStealingPool(int parallelism, String pooName) {
        return new ForkJoinPool(parallelism,
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                null, true);
    }

    /**
     * newSingleThreadScheduledExecutor
     * <p>
     * 参考：
     * {@link java.util.concurrent.Executors#newSingleThreadScheduledExecutor()} }
     */
    public static ScheduledExecutorService newSingleThreadScheduledExecutor(String pooName) {
        return Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * newThreadPerTaskExecutor
     * 参考：
     * {@link java.util.concurrent.Executors#newThreadPerTaskExecutor(ThreadFactory)} }
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
     * 参考：
     * {@link Executors#newVirtualThreadPerTaskExecutor()} }
     */
//    public static ExecutorService newVirtualThreadPerTaskExecutor(String pooName) {
//        return Executors.newVirtualThreadPerTaskExecutor();
//    }


}
