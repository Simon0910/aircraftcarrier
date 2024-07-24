package com.aircraftcarrier.framework.concurrent.locks;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class SynchronizedKey {
    private static final ConcurrentHashMap<String, LockWrapper> LOCKS = new ConcurrentHashMap<>();

    private SynchronizedKey() {
    }

    public static <T> T synchronizeOn(String lockKey, Supplier<T> action) {
        LockWrapper lockWrapper = LOCKS.computeIfAbsent(lockKey, k -> new LockWrapper());
        lockWrapper.lock();
        try {
            return action.get();
        } finally {
            lockWrapper.unlock();
            if (lockWrapper.canBeRemoved()) {
                LOCKS.remove(lockKey, lockWrapper);
            }
        }
    }

    public static void voidSynchronizeOn(String lockKey, Runnable action) {
        synchronizeOn(lockKey, () -> {
            action.run();
            return null;
        });
    }

    private static class LockWrapper {
        private final ReentrantLock lock = new ReentrantLock();

        public void lock() {
            lock.lock();
        }

        public void unlock() {
            lock.unlock();
        }

        public boolean canBeRemoved() {
            return !lock.isLocked() && !lock.hasQueuedThreads();
        }
    }
}


