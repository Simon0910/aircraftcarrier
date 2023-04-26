package com.aircraftcarrier.framework.concurrent;

import com.aircraftcarrier.framework.support.trace.MdcRunnableDecorator;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 创建自定义名称线程池
 */
public class NamedThreadFactory implements ThreadFactory {
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

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, new MdcRunnableDecorator(r), namePrefix + threadNumber.getAndIncrement(), 0);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}
