package com.aircraftcarrier.framework.concurrent;

import com.aircraftcarrier.framework.exception.ThreadException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * ThreadPoolUtil
 *
 * @author liuzhipeng
 */
@Slf4j
public class ThreadPoolUtil {

    /**
     * 私有
     */
    private ThreadPoolUtil() {
    }

    /**
     * 默认守护线程，无需手动关闭
     * {@link ForkJoinPool#commonPool(); }
     * {@link Executors#newWorkStealingPool(int) }
     */
    private static ForkJoinPool newDefaultForkJoinPool() {
        return ForkJoinPool.commonPool();
    }

    private static ExecutorService newDefaultExecutorService() {
        return ExecutorUtil.newCachedThreadPoolBlock(100, "default");
    }


    /**
     * newThreadFactory
     *
     * @param poolName poolName
     * @return NamedThreadFactory
     */
    public static NamedThreadFactory newThreadFactory(String poolName) {
        return new NamedThreadFactory(poolName);
    }


    public static ThreadPoolExecutor.AbortPolicy newAbortPolicy() {
        return new ThreadPoolExecutor.AbortPolicy();
    }

    public static ThreadPoolExecutor.CallerRunsPolicy newCallerRunsPolicy() {
        return new ThreadPoolExecutor.CallerRunsPolicy();
    }

    public static ThreadPoolExecutor.DiscardPolicy newDiscardPolicy() {
        return new ThreadPoolExecutor.DiscardPolicy();
    }

    public static ThreadPoolExecutor.DiscardOldestPolicy newDiscardOldestPolicy() {
        return new ThreadPoolExecutor.DiscardOldestPolicy();
    }

    /**
     * newDiscardPolicyPlus
     *
     * @return DiscardPolicyPlus
     */
    public static DiscardPolicyPlus newDiscardPolicyPlus() {
        return new DiscardPolicyPlus();
    }

    /**
     * newBlockPolicy
     *
     * @return BlockPolicy
     */
    public static BlockPolicy newBlockPolicy() {
        return new ThreadPoolUtil.BlockPolicy();
    }

    /**
     * submit
     *
     * @param executor executor
     * @param task     task
     * @return Future<?>
     */
    public static Future<?> submit(ExecutorService executor, Runnable task) {
        return executor.submit(task);
    }

    /**
     * submit
     *
     * @param executor executor
     * @param task     task
     * @param <T>      T
     * @return Future<T>
     */
    public static <T> Future<T> submit(ExecutorService executor, Callable<T> task) {
        return executor.submit(task);
    }


    // =========================invoke action==============================

    /**
     * executeAllVoid
     *
     * @param action action
     */
    public static void invokeVoid(RecursiveAction action) {
        ForkJoinPool forkJoinPool = newDefaultForkJoinPool();
        try {
            invokeVoid(forkJoinPool, action);
        } finally {
            forkJoinPool.shutdown();
        }
    }


    /**
     * executeAllVoid
     *
     * @param action      action
     * @param parallelism parallelism
     */
    public static void invokeVoid(RecursiveAction action, int parallelism) {
        ForkJoinPool forkJoinPool = ExecutorUtil.newWorkStealingPool(parallelism, "action");
        try {
            invokeVoid(forkJoinPool, action);
        } finally {
            forkJoinPool.shutdown();
        }
    }


    /**
     * executeAllVoid
     *
     * @param forkJoinPool forkJoinPool
     * @param action       action
     */
    public static void invokeVoid(ForkJoinPool forkJoinPool, RecursiveAction action) {
        forkJoinPool.invoke(action);
    }


    /**
     * executeVoid
     *
     * @param callableVoid callableVoid
     */
    public static void invokeVoid(CallableVoid callableVoid) {
        ExecutorService executorService = newDefaultExecutorService();
        try {
            invokeVoid(executorService, callableVoid);
        } finally {
            executorService.shutdown();
        }
    }


    /**
     * executeVoid
     *
     * @param executor     executor
     * @param callableVoid callableVoid
     */
    public static void invokeVoid(ExecutorService executor, CallableVoid callableVoid) {
        invokeAllVoid(executor, List.of(callableVoid));
    }


    /**
     * invokeAllVoid
     *
     * @param asyncBatchActions asyncBatchActions
     */
    public static void invokeAllVoid(List<CallableVoid> asyncBatchActions) {
        ExecutorService executorService = newDefaultExecutorService();
        try {
            invokeAllVoid(executorService, asyncBatchActions);
        } finally {
            executorService.shutdown();
        }
    }


    /**
     * executeAllVoid
     *
     * @param executor          executor
     * @param asyncBatchActions asyncBatchActions
     */
    public static void invokeAllVoid(ExecutorService executor, List<CallableVoid> asyncBatchActions) {
        List<Callable<Void>> callables = new ArrayList<>(asyncBatchActions.size());
        for (CallableVoid task : asyncBatchActions) {
            callables.add(() -> {
                task.call();
                return null;
            });
        }

        invokeAll(executor, callables);
    }


    // =========================invoke task==============================

    /**
     * invoke
     *
     * @param task task
     * @param <V>  V
     * @return V
     */
    public static <V> V invoke(RecursiveTask<V> task) {
        ForkJoinPool forkJoinPool = newDefaultForkJoinPool();
        try {
            return invoke(forkJoinPool, task);
        } finally {
            forkJoinPool.shutdown();
        }
    }


    /**
     * invoke
     *
     * @param task        task
     * @param parallelism parallelism
     * @param <V>         V
     * @return V
     */
    public static <V> V invoke(RecursiveTask<V> task, int parallelism) {
        ForkJoinPool forkJoinPool = ExecutorUtil.newWorkStealingPool(parallelism, "task");
        try {
            return invoke(forkJoinPool, task);
        } finally {
            forkJoinPool.shutdown();
        }
    }


    /**
     * invoke
     *
     * @param forkJoinPool forkJoinPool
     * @param task         task
     * @param <V>          V
     * @return V
     */
    public static <V> V invoke(ForkJoinPool forkJoinPool, RecursiveTask<V> task) {
        return forkJoinPool.invoke(task);
    }


    /**
     * invoke
     *
     * @param callable callable
     * @param <T>      T
     * @return T
     */
    public static <T> T invoke(Callable<T> callable) {
        ExecutorService executorService = newDefaultExecutorService();
        try {
            return invoke(executorService, callable);
        } finally {
            executorService.shutdown();
        }
    }


    /**
     * invoke
     *
     * @param executor executor
     * @param callable callable
     * @param <T>      T
     * @return T
     */
    public static <T> T invoke(ExecutorService executor, Callable<T> callable) {
        List<T> list = invokeAll(executor, List.of(callable));
        return list.get(0);
    }


    /**
     * invokeTask
     *
     * @param callParallelTask callParallelTask
     * @param parallelism      parallelism
     * @param <T>              T
     * @param <V>              V
     * @return List<V>
     */
    public static <T, V> List<V> invokeTask(CallParallelTask<T, V> callParallelTask, int parallelism) {
        ExecutorService executor = ExecutorUtil.newWorkStealingPool(parallelism, "callParallelTask");
        try {
            return invokeTask(executor, callParallelTask);
        } finally {
            executor.shutdown();
        }
    }


    /**
     * invokeTask
     *
     * @param callParallelTask callParallelTask
     * @param parallelism      parallelism
     * @param taskName         taskName
     * @param <T>              T
     * @param <V>              V
     * @return List<V>
     */
    public static <T, V> List<V> invokeTask(CallParallelTask<T, V> callParallelTask, int parallelism, String taskName) {
        ExecutorService executor = ExecutorUtil.newCachedThreadPoolBlock(parallelism, "invokeTask-" + taskName);
        try {
            return invokeTask(executor, callParallelTask);
        } finally {
            executor.shutdown();
        }
    }


    /**
     * invokeTask
     *
     * @param executor         executor
     * @param callParallelTask callParallelTask
     * @param <T>              T
     * @param <V>              V
     * @return List<V>
     */
    public static <T, V> List<V> invokeTask(ExecutorService executor, CallParallelTask<T, V> callParallelTask) {
        return invokeAll(executor, callParallelTask.getTaskList());
    }


    /**
     * invokeAll
     *
     * @param callables callables
     * @param <T>       T
     * @return List<T>
     */
    public static <T> List<T> invokeAll(List<Callable<T>> callables) {
        ExecutorService executorService = newDefaultExecutorService();
        try {
            return invokeAll(executorService, callables);
        } finally {
            executorService.shutdown();
        }
    }


    /**
     * invokeAll
     *
     * @param executor  executor
     * @param callables callables
     * @param <T>       T
     * @return List<T>
     */
    public static <T> List<T> invokeAll(ExecutorService executor, List<Callable<T>> callables) {
        return invokeAllTimeout(executor, callables, false, Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
    }


    /**
     * invokeAllTimeout
     * 适合并行查询，不适合并行更新（因为无法控制多个线程在一个事务）
     *
     * <p>
     * jdk19: {@link AbstractExecutorService#invokeAll(Collection, long, TimeUnit)}
     *
     * <a href="https://cloud.tencent.com/developer/article/1330450">...</a>
     * <a href="https://bugs.openjdk.org/browse/JDK-8286463">...</a>
     * <a href="https://bugs.openjdk.org/browse/JDK-8160037?focusedCommentId=13964474&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-13964474">...</a>
     * <p>
     * Doug Lea:
     * I agree with Martin. The behavior matches the specifications
     * -- shutdownNow returns the list of tasks, that the user should cancel if appropriate (that's why they are returned).
     */
    public static <T> List<T> invokeAllTimeout(ExecutorService executor,
                                               List<Callable<T>> tasks,
                                               boolean ignoreException,
                                               long timeout, TimeUnit unit) {
        if (tasks == null) {
            throw new NullPointerException();
        }

        final long nanos = unit.toNanos(timeout);
        final long deadline = System.nanoTime() + nanos;
        ArrayList<Future<T>> futures = new ArrayList<>(tasks.size());
        List<T> resultList = new ArrayList<>(tasks.size());
        RuntimeException runtimeException;
        int j = 0;
        breakOut:
        try {
            final int size = tasks.size();

            // Interleave time checks and calls to execute in case
            // executor doesn't have any/much parallelism.
            for (int i = 0; i < size; i++) {
                if (((i == 0) ? nanos : deadline - System.nanoTime()) <= 0L) {
                    runtimeException = new ThreadException("execute [" + i + "] TimeoutException ");
                    break breakOut;
                }
                futures.add(executor.submit(tasks.get(i)));
            }

            for (; j < size; j++) {
                Future<T> f = futures.get(j);
                try {
                    T result = f.get(deadline - System.nanoTime(), NANOSECONDS);
                    resultList.add(result);
                } catch (TimeoutException e) {
                    // TimeoutException | InterruptedException ：子线程还活着，子线程判断Thread.currentThread().isInterrupted()自己停止
                    log.error("Future got [{}] TimeoutException ", j, e);
                    f.cancel(true);
                    if (!ignoreException) {
                        runtimeException = new ThreadException("Future got [" + j + "] TimeoutException ", e);
                        break breakOut;
                    }
                } catch (InterruptedException e) {
                    log.error("Future got [{}] InterruptedException. ", j, e);
                    f.cancel(true);
                    Thread.currentThread().interrupt();
                    if (!ignoreException) {
                        runtimeException = new ThreadException("Future got [" + j + "] InterruptedException ", e);
                        break breakOut;
                    }
                } catch (CancellationException e) {
                    // CancellationException | ExecutionException： 子线程死了
                    log.error("Future got [{}] CancellationException.", j, e);
                    if (!ignoreException) {
                        runtimeException = new ThreadException("Future got [" + j + "] CancellationException ", e);
                        break breakOut;
                    }
                } catch (ExecutionException e) {
                    log.error("Future got [{}] ExecutionException. ", j, e);
                    if (!ignoreException) {
                        runtimeException = new ThreadException("Future got [" + j + "] ExecutionException ", e);
                        break breakOut;
                    }
                }
            }
            return resultList;
        } catch (Throwable t) {
            log.error("invokeAllTimeout error: ", t);
            cancelAll(futures);
            throw t;
        }
        // Timed out before all the tasks could be completed; cancel remaining
        cancelAll(futures, j);
        if (!ignoreException) {
            throw runtimeException;
        }
        return resultList;
    }


    // ======================private==============================

    private static <T> void cancelAll(ArrayList<Future<T>> futures) {
        cancelAll(futures, 0);
    }

    /**
     * Cancels all futures with index at least j.
     */
    private static <T> void cancelAll(ArrayList<Future<T>> futures, int j) {
        for (int size = futures.size(); j < size; j++) {
            futures.get(j).cancel(true);
        }
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
     * DiscardPolicyPlus
     */
    private static class DiscardPolicyPlus implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (executor.isShutdown()) {
                return;
            }

            if (r instanceof Future) {
                // 丢弃Future
                log.warn("DiscardPolicyNew ...");
                ((Future<?>) r).cancel(true);
            }
        }
    }

    /**
     * BlockPolicy
     */
    private static class BlockPolicy implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (executor.isShutdown() || Thread.currentThread().isInterrupted()) {
                return;
            }

            try {
                // 核心改造点，将blockingqueue的offer改成put阻塞提交
                executor.getQueue().put(r);
            } catch (InterruptedException ignore) {
                log.warn("Block Task " + r + " rejected from Interrupted");
                Thread.currentThread().interrupt();
            }
        }
    }


}
