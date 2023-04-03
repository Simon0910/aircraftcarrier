package com.aircraftcarrier.framework.exceltask;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhipengliu
 */
@Slf4j
public class ThreadPoolUtil {

    private ThreadPoolUtil() {
    }

    public static NamedThreadFactory newThreadFactory(String poolName) {
        return new NamedThreadFactory(poolName);
    }

    public static BlockPolicy newBlockPolicy() {
        return new BlockPolicy();
    }

    /**
     * 获取 生成当前线程的编号
     *
     * @return String
     */
    public static String getThreadNo() {
        return Thread.currentThread().getName().substring(Thread.currentThread().getName().lastIndexOf("-") + 1);
    }

    /**
     * 睡眠 （毫秒）
     *
     * @param timeout timeout
     */
    public static void sleepMilliseconds(long timeout) throws InterruptedException {
        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    /**
     * 睡眠 （秒）
     *
     * @param timeout timeout
     */
    public static void sleepSeconds(long timeout) throws InterruptedException {
        try {
            TimeUnit.SECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    /**
     * 创建自定义名称线程池
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        NamedThreadFactory(String poolName) {
            @SuppressWarnings("removal")
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" + poolNumber.getAndIncrement() + "-" + poolName + "-thread-";
        }

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
     * 自定义拒绝策略对象
     *
     * @see ThreadPoolExecutor.CallerRunsPolicy
     * @see ThreadPoolExecutor.AbortPolicy
     * @see ThreadPoolExecutor.DiscardPolicy
     * @see ThreadPoolExecutor.DiscardOldestPolicy
     */
    private static class BlockPolicy implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            // 核心改造点，将blockingqueue的offer改成put阻塞提交
            try {
                if (executor.isShutdown()) {
                    return;
                }
                executor.getQueue().put(r);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RejectedExecutionException("Block Task " + r + " rejected from " + e);
            }
        }
    }
}
