package com.aircraftcarrier.framework.concurrent;

import com.aircraftcarrier.framework.exception.ThreadException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadPoolUtil
 *
 * @author liuzhipeng
 */
@Slf4j
public class ThreadPoolUtil {

    /**
     * 每个线程等待多久 （秒）
     */
    private static final int PER_WAIT_TIMEOUT = 10000;
    /**
     * 默认守护线程，无需手动关闭
     */
    private static final ForkJoinPool DEFAULT_THREAD_POOL = new ForkJoinPool(
            Runtime.getRuntime().availableProcessors(),
            ForkJoinPool.defaultForkJoinWorkerThreadFactory,
            null, true);

    private ThreadPoolUtil() {
    }

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
     * DiscardPolicy
     */
    public static DiscardPolicyPlus newDiscardPolicyPlus() {
        return new DiscardPolicyPlus();
    }

    public static ThreadPoolUtil.BlockPolicy newBlockPolicy() {
        return new ThreadPoolUtil.BlockPolicy();
    }

    /**
     * executeVoid
     */
    public static void invokeVoid(CallableVoid callableVoid) {
        invokeVoid(DEFAULT_THREAD_POOL, callableVoid);
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
        ExecutorUtil.newWorkStealingPool(parallelism, "recursiveAction-pool").invoke(task);
    }

    /**
     * executeAllVoid
     */
    public static void invokeAllVoid(List<CallableVoid> asyncBatchTasks) {
        invokeAllVoid(DEFAULT_THREAD_POOL, asyncBatchTasks);
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
        return invoke(DEFAULT_THREAD_POOL, callable);
    }

    /**
     * execute
     */
    public static <T> T invoke(ExecutorService executor, Callable<T> callable) {
        List<T> list = invokeAll(executor, List.of(callable));
        return list.get(0);
    }

    public static <T> T invokeTime(Callable<T> callable, long timeout) {
        return invokeTimeout(DEFAULT_THREAD_POOL, callable, timeout);
    }

    public static <T> T invokeTimeout(ExecutorService executor, Callable<T> callable, long timeout) {
        List<T> list = invokeAllTimeout(executor, List.of(callable), timeout);
        return list.get(0);
    }

    /**
     * execute
     */
    public static <V> V invokeTask(RecursiveTask<V> task, int parallelism, String taskName) {
        ForkJoinPool forkJoinPool = ExecutorUtil.newWorkStealingPool(parallelism, taskName + "-recursiveTask-pool");
        return invokeTask(forkJoinPool, task);
    }

    /**
     * execute
     */
    public static <V> V invokeTask(ForkJoinPool forkJoinPool, RecursiveTask<V> task) {
        return forkJoinPool.invoke(task);
    }

    /**
     * execute
     */
    public static <T, V> List<V> invokeTask(CallApiParallelTask<T, V> task, int parallelism, String taskName) {
        ExecutorService executorService = ExecutorUtil.newCachedThreadPoolMax(taskName + "-parallelTask", parallelism, new ThreadPoolExecutor.CallerRunsPolicy());
        return invokeTask(executorService, task);
    }

    /**
     * execute
     */
    public static <T, V> List<V> invokeTask(ExecutorService executor, CallApiParallelTask<T, V> task) {
        List<Callable<V>> taskList = task.getTaskList();
        return invokeAll(executor, taskList);
    }

    public static <T, V> List<V> invokeTaskTimeout(CallApiParallelTask<T, V> task, int parallelism, long timeout, String taskName) {
        ExecutorService executorService = ExecutorUtil.newCachedThreadPoolMax(taskName + "-parallelTask", parallelism, new ThreadPoolExecutor.CallerRunsPolicy());
        return invokeTaskTimeout(executorService, task, timeout);
    }

    public static <T, V> List<V> invokeTaskTimeout(ExecutorService executor, CallApiParallelTask<T, V> task, long timeout) {
        List<Callable<V>> taskList = task.getTaskList();
        return invokeAllTimeout(executor, taskList, timeout);
    }

    /**
     * executeAll
     */
    public static <T> List<T> invokeAll(List<Callable<T>> asyncBatchTasks) {
        return invokeAll(DEFAULT_THREAD_POOL, asyncBatchTasks);
    }

    /**
     * executeAll
     */
    public static <T> List<T> invokeAll(ExecutorService executor, List<Callable<T>> asyncBatchTasks) {
        return invokeAll(executor, asyncBatchTasks, false);
    }

    public static <T> List<T> invokeAll(ExecutorService executor, List<Callable<T>> tasks, boolean ignoreFail) {
        return invokeAllTimeout(executor, tasks, ignoreFail, Integer.MAX_VALUE);
    }

    /**
     * executeAll
     */
    public static <T> List<T> invokeAllTimeout(List<Callable<T>> asyncBatchTasks, long timeout) {
        return invokeAllTimeout(DEFAULT_THREAD_POOL, asyncBatchTasks, timeout);
    }

    public static <T> List<T> invokeAllTimeout(ExecutorService executor, List<Callable<T>> asyncBatchTasks, long timeout) {
        return invokeAllTimeout(executor, asyncBatchTasks, false, timeout);
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
    public static <T> List<T> invokeAllTimeout(ExecutorService executor, List<Callable<T>> tasks,
                                               boolean ignoreFail, long timeout) {
        if (tasks == null) {
            throw new NullPointerException();
        }

        if (timeout < 10) {
            timeout = PER_WAIT_TIMEOUT;
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
                    T result = f.get(timeout, TimeUnit.MILLISECONDS);
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

    private static class DiscardPolicyPlus implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (executor.isShutdown()) {
                return;
            }

            if (r instanceof Future) {
                // 丢弃Future
                log.info("DiscardPolicyNew ...");
                ((Future<?>) r).cancel(true);
            }
        }

    }

    private static class BlockPolicy implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (executor.isShutdown()) {
                return;
            }

            try {
                // 核心改造点，将blockingqueue的offer改成put阻塞提交
                executor.getQueue().put(r);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RejectedExecutionException("Block Task " + r + " rejected from " + e);
            }
        }
    }


}
