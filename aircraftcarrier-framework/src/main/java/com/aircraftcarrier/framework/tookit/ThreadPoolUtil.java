package com.aircraftcarrier.framework.tookit;

import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.aircraftcarrier.framework.concurrent.CallableVoid;
import com.aircraftcarrier.framework.concurrent.DiscardPolicyRejectedExecutionHandler;
import com.aircraftcarrier.framework.exception.ThreadException;
import com.aircraftcarrier.framework.support.trace.TraceThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
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
     * 线程空闲时间
     */
    private static final int KEEP_ALIVE_TIME = 10;

    /**
     * 默认队列长度
     */
    private static final int QUEUE_SIZE = 1024;

    /**
     * 每个线程等待多久 （秒）
     */
    private static final int perWaitTimeout = 10;


    /**
     * 默认守护线程，无需手动关闭
     */
    private static final ForkJoinPool DEFAULT_THREAD_POOL = new ForkJoinPool(
            Runtime.getRuntime().availableProcessors(),
            ForkJoinPool.defaultForkJoinWorkerThreadFactory,
            null, true);


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
    private static ThreadFactory buildThreadFactory(String pooName, String suffix) {
        return ThreadFactoryBuilder
                .create()
                .setDaemon(false)
                .setNamePrefix(pooName + suffix)
                .build();
    }

    /**
     * DiscardPolicy
     */
    private static DiscardPolicyRejectedExecutionHandler buildDiscardPolicy() {
        return new DiscardPolicyRejectedExecutionHandler();
    }


    /**
     * 固定线程池 默认上限50000缓冲任务（防止无限创建队列任务oom） 多余的请求同步阻塞 （不丢弃任务）
     * <p>
     * 参考：
     * {@link java.util.concurrent.Executors#newFixedThreadPool(int)} }
     */
    public static ExecutorService newFixedThreadPool(int nThreads, String pooName) {
        return newFixedThreadPool(nThreads, pooName + "-caller", new LinkedBlockingQueue<>(QUEUE_SIZE), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * 固定线程池 不需要队列（防止无限创建队列任务oom） 多余的请求直接丢弃
     * <p>
     * 使用场景：
     * 1. newFixedThreadPool(1,"xxx-task"); // 开启一个后台监控任务
     */
    public static ExecutorService newFixedThreadPoolDiscard(int nThreads, String pooName) {
        return newFixedThreadPool(nThreads, pooName + "-discard", new SynchronousQueue<>(), buildDiscardPolicy());
    }

    public static ExecutorService newFixedThreadPool(int nThreads, String pooName, BlockingQueue<Runnable> blockingQueue, RejectedExecutionHandler reject) {
        return new TraceThreadPoolExecutor(
                // 固定大小
                nThreads, nThreads,
                0L, TimeUnit.SECONDS,
                blockingQueue,
                buildThreadFactory(pooName, "-fix-pool-"),
                reject);
    }

    /**
     * 缓存线程池 10秒空闲时间(防止无限创建缓存线程oom) 同步队列（默认队列就排队串行了！！！） 默认拒绝策略
     * <p>
     * 参考：
     * {@link java.util.concurrent.Executors#newCachedThreadPool() }
     * {@link cn.hutool.core.thread.ExecutorBuilder#build(cn.hutool.core.thread.ExecutorBuilder) }
     */
    public static ExecutorService newCachedThreadPool(String pooName) {
        return newCachedThreadPoolMax(pooName, Integer.MAX_VALUE, new ThreadPoolExecutor.AbortPolicy());
    }

    public static ExecutorService newCachedThreadPoolMax(String pooName, int maxSize) {
        return newCachedThreadPoolMax(pooName, maxSize, new ThreadPoolExecutor.AbortPolicy());
    }

    public static ExecutorService newCachedThreadPoolMax(String pooName, int maxSize, RejectedExecutionHandler reject) {
        return new TraceThreadPoolExecutor(
                // 指定大小
                0, maxSize,
                KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                buildThreadFactory(pooName, "-cached-pool-"),
                reject);
    }

    /**
     * 缓存线程池 最大nThreads线程大小(防止无限创建缓存线程oom) 同步队列 多余的请求直接丢弃
     * <p>
     * 使用场景：
     * 1. newCachedThreadPool(1,"refresh-token"); // 提前1小时，派一个线程取刷新token
     */
    public static ExecutorService newCachedThreadPoolDiscard(int nThreads, String pooName) {
        return new TraceThreadPoolExecutor(
                // 固定大小
                0, nThreads,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                buildThreadFactory(pooName, "-cached-discard-pool-"),
                // 忽略其他请求
                buildDiscardPolicy());
    }

    /**
     * 单线程执行器 1个线程串行执行所有任务 默认上限50000缓冲任务（防止无限创建队列任务oom）多余的请求同步阻塞 （不丢弃任务）
     * 参考：
     * {@link java.util.concurrent.Executors#newSingleThreadExecutor() }
     */
    public static ExecutorService newSingleThreadExecutor(String pooName) {
        return ExecutorBuilder.create()
                .setCorePoolSize(1).setMaxPoolSize(1)
                .setKeepAliveTime(0L, TimeUnit.MILLISECONDS)
                .setWorkQueue(new LinkedBlockingQueue<>(QUEUE_SIZE))
                .setThreadFactory(buildThreadFactory(pooName, "-single-caller-pool-"))
                .setHandler(new ThreadPoolExecutor.CallerRunsPolicy())
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
                .setThreadFactory(buildThreadFactory(pooName, "-single-discard-pool-"))
                .setHandler(buildDiscardPolicy())
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

    /**
     * executeVoid
     */
    public static void invokeVoid(CallableVoid callableVoid) {
        invokeAllVoid(DEFAULT_THREAD_POOL, List.of(callableVoid));
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
    public static void invokeVoid(RecursiveAction task) {
        DEFAULT_THREAD_POOL.invoke(task);
    }

    /**
     * executeAllVoid
     */
    public static void invokeVoid(RecursiveAction task, int parallelism) {
        ThreadPoolUtil.newWorkStealingPool(parallelism, "recursiveAction-pool").invoke(task);
    }

    /**
     * executeAllVoid
     */
    public static void invokeAllVoid(List<CallableVoid> asyncBatchTasks) {
        invokeAllVoid(DEFAULT_THREAD_POOL, asyncBatchTasks, false);
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
    public static void invokeAllVoid(ExecutorService executor, List<CallableVoid> asyncBatchTasks,
                                     boolean ignoreFail) {
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
        List<T> list = invokeAll(DEFAULT_THREAD_POOL, List.of(callable));
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
     * execute
     */
    public static <V> V invokeTask(RecursiveTask<V> task, ForkJoinPool forkJoinPool) {
        return forkJoinPool.invoke(task);
    }

    /**
     * execute
     */
    public static <V> V invokeTask(RecursiveTask<V> task, int parallelism, String taskName) {
        return ThreadPoolUtil.newWorkStealingPool(parallelism, taskName + "-recursiveTask-pool").invoke(task);
    }

    /**
     * execute
     */
    public static <T, V> List<V> invokeTask(ExecutorService executor, CallApiParallelTask<T, V> task) {
        List<Callable<V>> taskList = task.getTaskList();
        return invokeAll(executor, taskList);
    }

    /**
     * execute
     */
    public static <T, V> List<V> invokeTask(CallApiParallelTask<T, V> task, int parallelism, String taskName) {
        List<Callable<V>> taskList = task.getTaskList();
        return invokeAll(ThreadPoolUtil.newCachedThreadPoolMax(taskName + "-parallelTask", parallelism, new ThreadPoolExecutor.CallerRunsPolicy()), taskList);
    }

    /**
     * executeAll
     */
    public static <T> List<T> invokeAll(List<Callable<T>> asyncBatchTasks) {
        return invokeAll(DEFAULT_THREAD_POOL, asyncBatchTasks, false);
    }

    /**
     * executeAll
     */
    public static <T> List<T> invokeAll(ExecutorService executor, List<Callable<T>> asyncBatchTasks) {
        return invokeAll(executor, asyncBatchTasks, false);
    }

    /**
     * executeAll Ignore Fail
     * <a href="https://cloud.tencent.com/developer/article/1330450">...</a>
     * <a href="https://bugs.openjdk.org/browse/JDK-8286463">...</a>
     * <a href="https://bugs.openjdk.org/browse/JDK-8160037?focusedCommentId=13964474&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-13964474">...</a>
     * <p>
     * Doug Lea:
     * I agree with Martin. The behavior matches the specifications
     * -- shutdownNow returns the list of tasks, that the user should cancel if appropriate (that's why they are returned).
     */
    public static <T> List<T> invokeAll(ExecutorService executor, List<Callable<T>> tasks,
                                        boolean ignoreFail) {
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
            for (int i = 0, size = futures.size(); i < size; i++) {
                Future<T> f = futures.get(i);
                try {
                    /**
                     * {@link org.elasticsearch.common.util.concurrent.FutureUtils.get(java.util.concurrent.Future<T>, long, java.util.concurrent.TimeUnit)}
                     * {@link java.util.concurrent.AbstractExecutorService#invokeAll(java.util.Collection) }
                     */
                    T result = f.get(perWaitTimeout, TimeUnit.SECONDS);
                    resultList.add(result);
                } catch (CancellationException e) {
                    // CancellationException | ExecutionException： 子线程死了
                    // InterruptedException | TimeoutException：子线程还活着，子线程判断Thread.currentThread().isInterrupted()自己停止
                    f.cancel(true);
                    if (!ignoreFail) {
                        throw new ThreadException("Future got [" + i + "] CancellationException: " + e);
                    } else {
                        log.error("get [{}] CancellationException: ", i, e);
                    }
                } catch (ExecutionException e) {
                    f.cancel(true);
                    if (!ignoreFail) {
                        throw new ThreadException("Future got [" + i + "] ExecutionException: ", e);
                    } else {
                        log.error("get [{}] ExecutionException ", i, e);
                    }
                } catch (TimeoutException e) {
                    f.cancel(true);
                    if (!ignoreFail) {
                        throw new ThreadException("Future got  [" + i + "] TimeoutException: ", e);
                    } else {
                        log.error("get [{}] TimeoutException ", i, e);
                    }
                } catch (InterruptedException e) {
                    f.cancel(true);
                    Thread.currentThread().interrupt();
                    if (!ignoreFail) {
                        throw new ThreadException("Future got [" + i + "] InterruptedException: ", e);
                    } else {
                        log.error("get [{}] InterruptedException: ", i, e);
                    }
                }
            }
            return resultList;
        } catch (Throwable t) {
            log.error("invokeAll error: ", t);
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
