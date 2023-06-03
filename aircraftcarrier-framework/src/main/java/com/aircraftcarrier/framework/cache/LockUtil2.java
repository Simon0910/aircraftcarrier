package com.aircraftcarrier.framework.cache;

import com.aircraftcarrier.framework.cache.suport.MyLockTemplate;
import com.aircraftcarrier.framework.exception.LockNotAcquiredException;
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

    private LockUtil2() {
    }

    private static String getKey(String lockKey) {
        return getEnv() + lockKey;
    }


    public static boolean tryLock(String lockKey) {
        lockKey = getKey(lockKey);
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
            throw new LockNotAcquiredException(e);
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
        lockKey = getKey(lockKey);
        ReentrantLock writeKeyLock = getWriteLock(lockKey);
        try {
            if (!writeKeyLock.tryLock(timeout, unit)) {
                log.info("timeout...");
                return false;
            }
            // continue get redis lock...
        } catch (InterruptedException e) {
            log.error("tryLockTimeout error interrupted key [{}] ", lockKey, e);
            Thread.currentThread().interrupt();
            return false;
        }

        boolean locked = false;
        long expire = 30000; // 默认30秒自动失效
        long acquireTimeout = unit.toMillis(timeout);
        // 假设百分之99的场景的并发key, 过几毫秒就可以成功获取！ 超时之前最多获取 n次: retryInterval = acquireTimeout / n;
        // 假设 timout = 12s, n = 3, 4s重试一次, 共重试3次
        // |--- --- --- ---|--- --- --- ---|--- --- --- ---|
        // 快速重试2次, 实际重试2次, 共重试4次
        // |---|---|--- --- --- ---|--- --- --- ---|--- ---
        long retryInterval = acquireTimeout / 3;
        int retryCount = 0;
        int fastRetryCount = 2;
        try {
            long start = System.currentTimeMillis();
            do {
                LockInfo lockInfo = getMyLockTemplate().lockPlus(lockKey, expire, acquireTimeout, retryInterval, null);
                if (lockInfo != null) {
                    THREAD_LOCAL.set(lockInfo);
                    locked = true;
                    return true;
                }
                if (retryCount < fastRetryCount) {
                    retryCount++;
                    TimeUnit.MILLISECONDS.sleep(10);
                } else {
                    TimeUnit.MILLISECONDS.sleep(retryInterval);
                }
            } while (System.currentTimeMillis() - start < acquireTimeout);
            log.info("tryLock timeout...");
            return false;
        } catch (InterruptedException e) {
            log.error("tryLock error interrupted key [{}] ", lockKey, e);
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.error("tryLock error key [{}] ", lockKey, e);
            throw new LockNotAcquiredException(e);
        } finally {
            if (!locked) {
                writeKeyLock.unlock();
                if (!writeKeyLock.hasQueuedThreads() && !writeKeyLock.isLocked()) {
                    log.info("tryLock timeout removeWriteLock [{}] ", lockKey);
                    removeWriteLock(lockKey);
                }
            }
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
        lockKey = getKey(lockKey);
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
            if (writeKeyLock != null) {
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
    }

    private static MyLockTemplate getMyLockTemplate() {
        return LockUtil2.ResourceHolder.myLockTemplate;
    }

    private static String getEnv() {
        return LockUtil2.ResourceHolder.ENV;
    }

    private static class ResourceHolder {
        public static final MyLockTemplate myLockTemplate = (MyLockTemplate) ApplicationContextUtil.getBean(LockTemplate.class); // This will be lazily initialised
        private static final String ENV = ApplicationContextUtil.getApplicationContext().getEnvironment().getActiveProfiles()[0] + ":";
    }
}
