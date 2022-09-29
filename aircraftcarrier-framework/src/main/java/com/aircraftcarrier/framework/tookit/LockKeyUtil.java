package com.aircraftcarrier.framework.tookit;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author lzp
 */
@Slf4j
public class LockKeyUtil {

    private static final String DEFAULT_KEY = "block";

    private LockKeyUtil() {
    }

    private static final Map<String, LockWrapper> LOCKS = MapUtil.newConcurrentHashMap(1024);

    public static void lock() {
        lock(DEFAULT_KEY);
    }

    public static void lock(String key) {
        LockWrapper lockWrapper = LOCKS.compute(key, (k, v) -> v == null ? new LockWrapper() : v);
        lockWrapper.lock.lock();
        lockWrapper.addThreadInQueue();
    }

    public static boolean tryLock() {
        return tryLock(DEFAULT_KEY);
    }

    public static boolean tryLock(String key) {
        LockWrapper lockWrapper = LOCKS.compute(key, (k, v) -> v == null ? new LockWrapper() : v);
        boolean b = lockWrapper.lock.tryLock();
        if (b) {
            lockWrapper.addThreadInQueue();
        }
        return b;
    }

    public static boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        return tryLock(DEFAULT_KEY, timeout, unit);
    }

    public static boolean tryLock(String key, long timeout, TimeUnit unit) throws InterruptedException {
        LockWrapper lockWrapper = LOCKS.compute(key, (k, v) -> v == null ? new LockWrapper() : v);
        boolean b = lockWrapper.lock.tryLock(timeout, unit);
        if (b) {
            lockWrapper.addThreadInQueue();
        }
        return b;
    }

    public static void unlock() {
        unlock(DEFAULT_KEY);
    }

    public static void unlock(String key) {
        LockWrapper lockWrapper = LOCKS.get(key);
        if (lockWrapper.removeThreadFromQueue() == 0) {
            // NB : We pass in the specific value to remove to handle the case where another thread would queue right before the removal
            LOCKS.remove(key, lockWrapper);
        }
        lockWrapper.lock.unlock();
    }

    private static class LockWrapper {
        private final Lock lock = new ReentrantLock();
        private final AtomicInteger numberOfThreadsInQueue = new AtomicInteger(1);

        private LockWrapper addThreadInQueue() {
            numberOfThreadsInQueue.incrementAndGet();
            return this;
        }

        private int removeThreadFromQueue() {
            return numberOfThreadsInQueue.decrementAndGet();
        }

    }
}