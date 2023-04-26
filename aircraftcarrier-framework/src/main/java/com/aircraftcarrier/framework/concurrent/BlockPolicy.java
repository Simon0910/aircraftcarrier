package com.aircraftcarrier.framework.concurrent;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 自定义拒绝策略对象
 *
 * @see ThreadPoolExecutor.CallerRunsPolicy
 * @see ThreadPoolExecutor.AbortPolicy
 * @see ThreadPoolExecutor.DiscardPolicy
 * @see ThreadPoolExecutor.DiscardOldestPolicy
 */
public class BlockPolicy implements RejectedExecutionHandler {
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