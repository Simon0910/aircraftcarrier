package com.aircraftcarrier.framework.concurrent;

import cn.hutool.core.thread.ExecutorBuilder;
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
import java.util.concurrent.atomic.AtomicInteger;


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
     * newThreadFactory
     * 参考：{@link Executors#defaultThreadFactory() }
     *
     * @param poolName poolName
     * @return NamedThreadFactory
     */
    public static NamedThreadFactory newNamedThreadFactory(String poolName) {
        return new  NamedThreadFactory(poolName);
    }

    /**
     * 固定线程池
     * 同时最多n个线程并发执行，多余请求阻塞等待，固定缓存1024个任务
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
     * 固定线程池
     * 同时最多n个线程并发执行，多余请求丢弃
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

    /**
     * 固定线程池
     * 相比 {@link Executors#newFixedThreadPool(int)} 可以指定线程池名称和日志追踪，可以防止队列容量OOM
     *
     * @param nThreads
     * @param pooName
     * @param blockingQueue
     * @param reject
     * @return
     */
    private static ExecutorService newFixedThreadPool(int nThreads,
                                                      String pooName,
                                                      BlockingQueue<Runnable> blockingQueue,
                                                      RejectedExecutionHandler reject) {
        return new TraceThreadPoolExecutor(
                // 固定大小
                nThreads, nThreads,
                0L, TimeUnit.SECONDS,
                blockingQueue,
                newNamedThreadFactory("fix-" + pooName),
                reject);
    }

    /**
     * 缓存线程池
     * 同时最多n个线程并发执行，多余请求阻塞等待，执行结束释放线程
     */
    public static ExecutorService newCachedThreadPoolBlock(int maxThreads, String pooName) {
        return newCachedThreadPool(
                "block-" + pooName,
                maxThreads,
                ThreadPoolUtil.newBlockPolicy());
    }


    /**
     * 缓存线程池
     * 同时最多n个线程并发执行，多余请求丢弃，执行结束释放线程
     * <p>
     * 使用场景：
     * 1. newCachedThreadPool(1, "refresh-token"); // 提前1小时，派一个线程取刷新token
     */
    public static ExecutorService newCachedThreadPoolDiscard(int maxThreads, String pooName) {
        return newCachedThreadPool(
                "discard-" + pooName,
                maxThreads,
                ThreadPoolUtil.newDiscardPolicyPlus());
    }

    /**
     * 缓存线程池
     * 相比 {@link Executors#newCachedThreadPool() } 可以指定线程池名称和日志追踪，可以防止无限创建线程
     *
     * @param pooName    pooName
     * @param maxThreads maxThreads
     * @param reject     reject
     * @return ExecutorService
     */
    private static ExecutorService newCachedThreadPool(String pooName,
                                                       int maxThreads,
                                                       RejectedExecutionHandler reject) {
        return new TraceThreadPoolExecutor(
                // 指定大小
                0, maxThreads,
                10, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                newNamedThreadFactory("cached-" + pooName),
                reject);
    }

    /**
     * 单线程执行器
     * 相比 {@link Executors#newSingleThreadExecutor() } 可以指定线程池名称和日志追踪，可以防止队列容量OOM，多余的请求同步阻塞（不丢弃任务）
     *
     * @param pooName pooName
     * @return ExecutorService
     */
    public static ExecutorService newSingleThreadExecutorBlock(String pooName) {
        return ExecutorBuilder.create()
                .setCorePoolSize(1).setMaxPoolSize(1)
                .setKeepAliveTime(0L, TimeUnit.MILLISECONDS)
                .setWorkQueue(new LinkedBlockingQueue<>(1024))
                .setThreadFactory(newNamedThreadFactory("single-block-" + pooName))
                .setHandler(ThreadPoolUtil.newBlockPolicy())
                .buildFinalizable();
    }

    /**
     * 单线程执行器
     * 相比 {@link Executors#newSingleThreadExecutor() } 可以指定线程池名称和日志追踪，可以防止队列容量OOM，多余的请求丢弃
     * <p>
     * 使用场景：限流
     *
     * @param pooName pooName
     * @return ExecutorService
     */
    public static ExecutorService newSingleThreadExecutorDiscard(String pooName) {
        return ExecutorBuilder.create()
                .setCorePoolSize(1).setMaxPoolSize(1)
                .setKeepAliveTime(0L, TimeUnit.MILLISECONDS)
                .setWorkQueue(new SynchronousQueue<>())
                .setThreadFactory(newNamedThreadFactory("single-discard-" + pooName))
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
     * NamedThreadFactory
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        public NamedThreadFactory(String poolName) {
            @SuppressWarnings("removal")
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" + poolNumber.getAndIncrement() + "-" + poolName + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
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
