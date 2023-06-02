package com.aircraftcarrier.framework.cache;

import com.aircraftcarrier.framework.cache.suport.MyLockTemplate;
import com.aircraftcarrier.framework.tookit.ApplicationContextUtil;
import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author lzp
 * @since 2023/06/01 17:07
 */
@Slf4j
public class LockUtil2 {

    private static final ThreadLocal<LockInfo> THREAD_LOCAL = new ThreadLocal<>();
    private static final Map<String, ReentrantLock> LOCAL_LOCK_CACHE = Maps.newConcurrentMap();

    public static boolean tryLock(String lockKey) {
        long expire = 30000; // 默认30秒自动失效
        long acquireTimeout = 10;
        long retryInterval = 0;

        try {
            LockInfo lockInfo = getMyLockTemplate().lockPlus(lockKey, expire, acquireTimeout, retryInterval, null);
            if (lockInfo != null) {
                THREAD_LOCAL.set(lockInfo);
                return true;
            }
            log.info("not locked!");
            return false;
        } catch (Exception e) {
            log.error("tryLock error key [{}] ", lockKey, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * tryLock
     *
     * @param lockKey lockKey
     * @param timeout timeout
     * @param unit    unit
     * @return boolean
     */
    public static boolean tryLock(String lockKey, long timeout, TimeUnit unit) {
        long expire = 30000; // 默认30秒自动失效
        long acquireTimeout = unit.toMillis(timeout);
        long retryInterval = 20;

        ReentrantLock writeKeyLock = getWriteLock(lockKey);
        long start = System.currentTimeMillis();
        try {
            while (true) {
                boolean ok = writeKeyLock.tryLock(timeout, unit);
                if (ok) {
                    // continue get redis lock
                    break;
                }
                if (System.currentTimeMillis() - start >= acquireTimeout) {
                    log.info("timeout...");
                    return false;
                }
                log.info("retry...");
                TimeUnit.MILLISECONDS.sleep(retryInterval);
            }
        } catch (Exception e) {
            log.error("tryLockTimeout error key [{}] ", lockKey, e);
            return false;
        }

        try {
            if (System.currentTimeMillis() - start < acquireTimeout) {
                LockInfo lockInfo = getMyLockTemplate().lockPlus(lockKey, expire, acquireTimeout, retryInterval, null);
                if (lockInfo != null) {
                    THREAD_LOCAL.set(lockInfo);
                    return true;
                }
                TimeUnit.MILLISECONDS.sleep(retryInterval);
            }
            log.info("tryLock timeout...");
            writeKeyLock.unlock();
            if (!writeKeyLock.hasQueuedThreads() && !writeKeyLock.isLocked()) {
                log.info("tryLock timeout removeWriteLock [{}] ", lockKey);
                removeWriteLock(lockKey);
            }
            return false;
        } catch (Exception e) {
            log.error("tryLock error key [{}] ", lockKey, e);
            writeKeyLock.unlock();
            if (!writeKeyLock.hasQueuedThreads() && !writeKeyLock.isLocked()) {
                log.info("tryLock error removeWriteLock [{}] ", lockKey);
                removeWriteLock(lockKey);
            }
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static ReentrantLock getWriteLock(String lockKey) {
        return LOCAL_LOCK_CACHE.compute(lockKey, (k, v) -> v == null ? new ReentrantLock() : v);
    }

    private static void removeWriteLock(String lockKey) {
        LOCAL_LOCK_CACHE.remove(lockKey);
    }

    /**
     * unLock
     *
     * @param lockKey lockKey
     */
    public static void unLock(String lockKey) {
        try {
            LockInfo lockInfo = THREAD_LOCAL.get();
            if (lockInfo != null) {
                log.info("unLock key [{}] ", lockKey);
                THREAD_LOCAL.remove();
                getMyLockTemplate().releaseLock(lockInfo);
            }
        } catch (Exception e) {
            log.error("unLock error key [{}] ", lockKey, e);
        } finally {
            ReentrantLock writeKeyLock = LOCAL_LOCK_CACHE.get(lockKey);
            if (writeKeyLock == null) {
                return;
            }
            if (writeKeyLock.isHeldByCurrentThread()) {
                log.info("unLock writeKeyLock [{}] ", lockKey);
                writeKeyLock.unlock();
            }
            if (!writeKeyLock.hasQueuedThreads()) {
                log.info("unLock removeWriteLock [{}] ", lockKey);
                removeWriteLock(lockKey);
            }
        }
    }

    private static MyLockTemplate getMyLockTemplate() {
        return LockUtil2.ResourceHolder.myLockTemplate;
    }

    private static class ResourceHolder {
        public static MyLockTemplate myLockTemplate = (MyLockTemplate) ApplicationContextUtil.getBean(LockTemplate.class); // This will be lazily initialised
    }
}
