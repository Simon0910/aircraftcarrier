package com.aircraftcarrier.framework.support.trace;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * TraceThreadPoolExecutor 重写原生线程池(含:传递MDC上下文)
 * 可以提交jdk原生的Runnable
 *
 * @author liuzhipeng
 */
public class TraceThreadPoolExecutor extends ThreadPoolExecutor {
    public TraceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public TraceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public TraceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public TraceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    public void allowCoreThreadTimeOut(boolean value) {
        super.allowCoreThreadTimeOut(value);
    }

    @Override
    public void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException();
        }
        if (command instanceof Future) {
            // 谁进入execute，谁就才有isCancel状态，为了使command有状态，所以没有装饰
            // 1. 要么把有状态赋给command。2. 建议使用submit方法
            super.execute(command);
            return;
        }
        super.execute(new MdcRunnableDecorator(command));
    }

    @Override
    public Future<?> submit(Runnable task) {
        if (task == null) {
            throw new NullPointerException();
        }
        if (task instanceof RunnableFuture) {
            RunnableFuture<?> f = (RunnableFuture<?>) task;
            super.execute(f);
            return f;
        }
        RunnableFuture<Void> ftask = newTaskForTrace(task, null);
        super.execute(ftask);
        return ftask;
    }

    protected <T> RunnableFuture<T> newTaskForTrace(Runnable runnable, T value) {
        return new FutureTask<>(new MdcRunnableDecorator(runnable), value);
    }

}