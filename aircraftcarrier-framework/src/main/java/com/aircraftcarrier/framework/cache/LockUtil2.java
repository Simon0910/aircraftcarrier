package com.aircraftcarrier.framework.cache;

import com.aircraftcarrier.framework.cache.suport.MyLockTemplate;
import com.aircraftcarrier.framework.exception.LockNotAcquiredException;
import com.aircraftcarrier.framework.tookit.ApplicationContextUtil;
import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author lzp
 * @since 2023/06/01 17:07
 */
@Slf4j
public class LockUtil2 {
    private static final ThreadLocal<LockInfo> THREAD_LOCAL = new ThreadLocal<>();
    private static final Cache<String, ReentrantLock> LOCAL_LOCK_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.SECONDS)
            .initialCapacity(50000)
            .build();

    private LockUtil2() {
    }

    private static String getKey(String lockKey) {
        return getEnv() + lockKey;
    }


    public static boolean tryLock(String lockKey, long expire, TimeUnit unit) {
        return tryLock(lockKey, expire, 0, unit);
    }

    /**
     * tryLock
     *
     * @param lockKey lockKey
     * @param timeout timeout
     * @param expire  expire
     * @param unit    unit
     * @return boolean
     */
    public static boolean tryLock(String lockKey, long expire, long timeout, TimeUnit unit) {
        long start = System.currentTimeMillis();
        lockKey = getKey(lockKey);
        ReentrantLock writeKeyLock = null;
        boolean needUnlock = false;
        try {
            writeKeyLock = setAndGetWriteLock(lockKey);
            if (!writeKeyLock.tryLock(timeout, unit)) {
                log.info("timeout [{}]!", lockKey);
                return false;
            }
            // continue get redis lock...

            long acquireTimeout = unit.toMillis(timeout);
            // 假设百分之90的场景的并发key, 过几十毫秒就可以成功获取！
            int retryCount = 0;
            int maxFastRetryNum = 2;
            long retryInterval = 1000;
            boolean lastTime = false;
            long lastTimeRetryInterval;
            do {
                // 1次尝试取锁，2次快速取锁，3次重试取锁 | 后面每3秒获取一次 | 超时前获取一次
                if (retryCount < 6 || retryCount % 3 == 0 || lastTime) {
                    log.info("tryLock [{}]...", lockKey);
                    LockInfo lockInfo = getMyLockTemplate().lockPlus(lockKey, expire, acquireTimeout, 0, null);
                    if (lockInfo != null) {
                        THREAD_LOCAL.set(lockInfo);
                        return true;
                    } else if (lastTime) {
                        log.info("tryLock timeout [{}]", lockKey);
                        needUnlock = true;
                        return false;
                    }
                }
                if (retryCount < maxFastRetryNum) {
                    TimeUnit.MILLISECONDS.sleep(10);
                } else {
                    lastTime = (lastTimeRetryInterval = acquireTimeout - (System.currentTimeMillis() - start)) <= 1000;
                    if (lastTime) {
                        TimeUnit.MILLISECONDS.sleep(lastTimeRetryInterval - 10);
                    } else {
                        TimeUnit.MILLISECONDS.sleep(retryInterval);
                    }
                }
                retryCount++;
            } while (System.currentTimeMillis() - start < acquireTimeout);
            log.info("tryLock timeout [{}].", lockKey);
            needUnlock = true;
            return false;
        } catch (InterruptedException e) {
            needUnlock = true;
            log.error("tryLock error interrupted key [{}] ", lockKey, e);
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            needUnlock = true;
            log.error("tryLock error key [{}] ", lockKey, e);
            throw new LockNotAcquiredException(e);
        } finally {
            if (needUnlock && writeKeyLock != null) {
                writeKeyLock.unlock();
                if (!writeKeyLock.hasQueuedThreads() && !writeKeyLock.isLocked()) {
                    log.info("tryLock timeout removeWriteLock [{}] ", lockKey);
                    removeWriteLock(lockKey);
                }
            }
        }
    }

    @NotNull
    private static ReentrantLock setAndGetWriteLock(String lockKey) throws ExecutionException {
        return LOCAL_LOCK_CACHE.get(lockKey, ReentrantLock::new);
    }

    private static void removeWriteLock(String lockKey) {
        LOCAL_LOCK_CACHE.invalidate(lockKey);
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
                doUnLock(lockInfo, 1);
            }
        } catch (Exception e) {
            log.error("unLock error key [{}] ", lockKey, e);
        } finally {
            ReentrantLock writeKeyLock = LOCAL_LOCK_CACHE.getIfPresent(lockKey);
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

    private static boolean doUnLock(LockInfo lockInfo, int retry) {
        try {
            // 释放锁
            getMyLockTemplate().releaseLock(lockInfo);
            return true;
        } catch (Exception e) {
            log.warn("doUnLock {}", lockInfo.getLockKey(), e);
            // 网络异常
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }

            if (retry > 0) {
                retry--;
                return doUnLock(lockInfo, retry);
            } else {
                return false;
            }
        }
    }

    private static MyLockTemplate getMyLockTemplate() {
        if (LockUtil2.ResourceHolder.myLockTemplate == null) {
            LockUtil2.ResourceHolder.myLockTemplate = (MyLockTemplate) ApplicationContextUtil.getBean(LockTemplate.class);
        }
        return LockUtil2.ResourceHolder.myLockTemplate;
    }

    private static String getEnv() {
        if (LockUtil2.ResourceHolder.env == null) {
            LockUtil2.ResourceHolder.env = ApplicationContextUtil.getApplicationContext().getEnvironment().getActiveProfiles()[0] + ":";
        }
        return LockUtil2.ResourceHolder.env;
    }

    private static class ResourceHolder {
        public static MyLockTemplate myLockTemplate = getMyLockTemplate(); // This will be lazily initialised
        private static String env = getEnv();
    }
}
