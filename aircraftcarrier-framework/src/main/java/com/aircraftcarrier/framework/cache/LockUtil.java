package com.aircraftcarrier.framework.cache;

import com.aircraftcarrier.framework.cache.suport.MyLockTemplate;
import com.aircraftcarrier.framework.exception.FrameworkException;
import com.aircraftcarrier.framework.exception.LockNotAcquiredException;
import com.aircraftcarrier.framework.tookit.ApplicationContextUtil;
import com.aircraftcarrier.framework.tookit.SleepUtil;
import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author lzp
 */
@Slf4j
public class LockUtil {
    /**
     * 默认超时30s
     */
    private static final long EXPIRE = 30;

    /**
     * 等待多久 ms
     */
    private static final long ACQUIRE_TIMEOUT = -1;
    /**
     * 每次间隔时间 ms
     * -1：不睡眠
     */
    private static final long RETRY_INTERVAL = -1;
    private static final String DEFAULT_KEY = "block";
    private static final ThreadLocal<Map<String, LockInfo>> THREAD_LOCAL = new ThreadLocal<>();
    private static final Map<String, Thread> LOCK_RECORD = new ConcurrentHashMap<>();

    private static volatile MyLockTemplate myLockTemplate;

    static {
        LockUtil.myLockTemplate = (MyLockTemplate) ApplicationContextUtil.getBean(LockTemplate.class);
        WatchDog.getInstance().init(LOCK_RECORD);
    }

    private LockUtil() {
    }

    private static MyLockTemplate getMyLockTemplate() {
        if (LockUtil.myLockTemplate == null) {
            synchronized (LockUtil.class) {
                if (LockUtil.myLockTemplate == null) {
                    LockUtil.myLockTemplate = (MyLockTemplate) ApplicationContextUtil.getBean(LockTemplate.class);
                }
            }
        }
        return LockUtil.myLockTemplate;
    }

    public static void lock() throws LockNotAcquiredException {
        lock(DEFAULT_KEY, EXPIRE, ACQUIRE_TIMEOUT, RETRY_INTERVAL);
    }

    public static void lock(Serializable key) {
        lock(key, EXPIRE, ACQUIRE_TIMEOUT, RETRY_INTERVAL);
    }

    public static void lock(Serializable key, long expire) {
        lock(key, expire, ACQUIRE_TIMEOUT, RETRY_INTERVAL);
    }

    public static void lockTimeout(Serializable key, long acquireTimeout, long retryInterval) {
        lock(key, EXPIRE, acquireTimeout, retryInterval);
    }

    public static void lock(Serializable key, long expire, long acquireTimeout, long retryInterval) {
        if (!doLock(key, expire, acquireTimeout, retryInterval)) {
            throw new LockNotAcquiredException("the redis distributed lock was not acquired");
        }
    }

    public static Boolean tryLock() {
        return tryLock(DEFAULT_KEY, EXPIRE, ACQUIRE_TIMEOUT, RETRY_INTERVAL);
    }

    public static Boolean tryLock(Serializable key) {
        return tryLock(key, EXPIRE, ACQUIRE_TIMEOUT, RETRY_INTERVAL);
    }

    public static Boolean tryLock(Serializable key, long expire) {
        return tryLock(key, expire, ACQUIRE_TIMEOUT, RETRY_INTERVAL);
    }

    public static Boolean tryLockTimeout(Serializable key, long acquireTimeout, long retryInterval) {
        return tryLock(key, EXPIRE, acquireTimeout, retryInterval);
    }

    public static Boolean tryLock(Serializable key, long expire, long acquireTimeout, long retryInterval) {
        return doLock(key, expire, acquireTimeout, retryInterval);
    }

    private static boolean doLock(Serializable key, long expire, long acquireTimeout, long retryInterval) {
        String lockKey = String.valueOf(key);

        // 别的线程已经持有该锁，要重试吗？
        Thread thread = LOCK_RECORD.get(lockKey);
        if (thread != null && thread != Thread.currentThread()) {
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < acquireTimeout) {
                thread = LOCK_RECORD.get(lockKey);
                if (thread == null) {
                    break;
                }
                SleepUtil.sleepMilliseconds(retryInterval);
            }
            // 重试这么多次，还有别人持有该锁，就不请求redis了
            thread = LOCK_RECORD.get(lockKey);
            if (thread != null) {
                // 已经有别的线程加上锁了，不用再请求redis了 （单机版即使redis锁key自动失效了，也不用续期锁的有效期了，保证finally要移除登记记录，宕机无需考虑）
                log.warn("wait other key [{}]: Thread: {}", key, Thread.currentThread().getName());
                return false;
            }
        }

        Map<String, LockInfo> lockInfoMap = THREAD_LOCAL.get();
        LockInfo lockInfo;
        if (lockInfoMap != null && (lockInfo = lockInfoMap.get(lockKey)) != null) {
            // 可重入
            lockInfo.setAcquireCount(lockInfo.getAcquireCount() + 1);
            return true;
        }

        // 新锁
        LockInfo newLock = getMyLockTemplate().lockPlus(lockKey, expire * 1000, acquireTimeout, retryInterval, null);
        if (null == newLock) {
            log.info("not lock key [{}]: Thread: {}", key, Thread.currentThread().getName());
            return false;
        }
        // 登记记录
        LOCK_RECORD.put(lockKey, Thread.currentThread());
        WatchDog.getInstance().startUp();
        newLock.setAcquireCount(1);

        if (lockInfoMap != null) {
            // 当前线程新锁
            lockInfoMap.put(lockKey, newLock);
            return true;
        }

        // 当前线程第一个锁
        lockInfoMap = new HashMap<>(16);
        lockInfoMap.put(lockKey, newLock);
        THREAD_LOCAL.set(lockInfoMap);
        return true;
    }

    public static void unLock() {
        unLock(DEFAULT_KEY);
    }

    public static void unLock(Serializable key) {
        String lockKey = String.valueOf(key);
        Map<String, LockInfo> lockInfoMap = THREAD_LOCAL.get();
        if (lockInfoMap == null || lockInfoMap.isEmpty()) {
            THREAD_LOCAL.remove();
            return;
        }
        LockInfo lockInfo;
        if ((lockInfo = lockInfoMap.get(lockKey)) == null) {
            return;
        }

        int acquireCount = lockInfo.getAcquireCount();
        if (acquireCount > 1) {
            // 解锁次数还没有完成，还需要继续解锁
            lockInfo.setAcquireCount(--acquireCount);
            return;
        }

        // 最后一次解锁
        lockInfoMap.remove(lockKey);
        if (lockInfoMap.isEmpty()) {
            THREAD_LOCAL.remove();
        }
        // 移除登记记录，别的线程就可以从redis获取锁了
        Thread thread = LOCK_RECORD.get(lockKey);
        if (Thread.currentThread() == thread) {
            LOCK_RECORD.remove(lockKey);
        }

        // 先执行一次，失败重试3次
        log.info("doUnLock key [{}]: Thread: {}", lockKey, Thread.currentThread().getName());
        if (!doUnLock(lockInfo, 3)) {
            // 释放锁失败了，会停止续期吗？不会，因为已经移除登记记录
            throw new FrameworkException("释放锁异常");
        }
    }

    private static boolean doUnLock(LockInfo lockInfo, int retry) {
        try {
            // 释放锁
            getMyLockTemplate().releaseLock(lockInfo);
            return true;
        } catch (Exception e) {
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException ignored) {
            }

            if (retry > 0) {
                retry--;
                return doUnLock(lockInfo, retry);
            } else {
                return false;
            }
        }
    }
}
