package com.aircraftcarrier.framework.cache;

import com.aircraftcarrier.framework.cache.suport.MyLockTemplate;
import com.aircraftcarrier.framework.exception.FrameworkException;
import com.aircraftcarrier.framework.tookit.ApplicationContextUtil;
import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
    private static final long ACQUIRE_TIMEOUT = 0;
    /**
     * 每次间隔时间 ms
     * -1：不睡眠
     */
    private static final long RETRY_INTERVAL = -1;
    private static final String DEFAULT_KEY = "block";
    private static final MyLockTemplate LOCK_TEMPLATE;
    private static final ThreadLocal<Map<String, LockInfo>> THREAD_LOCAL = new ThreadLocal<>();
    private static final Map<String, Thread> LOCK_RECORD = new ConcurrentHashMap<>();

    static {
        LOCK_TEMPLATE = (MyLockTemplate) ApplicationContextUtil.getBean(LockTemplate.class);
        WatchDog.getInstance().init(LOCK_RECORD);
    }

    private LockUtil() {
    }

    public static void lock() throws TimeoutException {
        lock(DEFAULT_KEY, EXPIRE, ACQUIRE_TIMEOUT, RETRY_INTERVAL);
    }

    public static void lock(Serializable key) throws TimeoutException {
        lock(key, EXPIRE, ACQUIRE_TIMEOUT, RETRY_INTERVAL);
    }

    public static void lock(Serializable key, long expire) throws TimeoutException {
        lock(key, expire, ACQUIRE_TIMEOUT, RETRY_INTERVAL);
    }

    public static void lockTimeout(Serializable key, long acquireTimeout, long retryInterval) throws TimeoutException {
        lock(key, EXPIRE, acquireTimeout, retryInterval);
    }

    public static void lock(Serializable key, long expire, long acquireTimeout, long retryInterval) throws TimeoutException {
        try {
            doLock(key, expire, acquireTimeout, retryInterval, false);
        } catch (FrameworkException e) {
            throw new TimeoutException(e.getMessage());
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
        return doLock(key, expire, acquireTimeout, retryInterval, true);
    }

    private static boolean doLock(Serializable key, long expire, long acquireTimeout, long retryInterval, boolean isTry) {
        String lockKey = String.valueOf(key);

        Map<String, LockInfo> lockInfoMap = THREAD_LOCAL.get();
        LockInfo lockInfo;
        if (lockInfoMap != null && (lockInfo = lockInfoMap.get(lockKey)) != null) {
            // 可重入
            lockInfo.setAcquireCount(lockInfo.getAcquireCount() + 1);
            return true;
        }

        Thread thread = LOCK_RECORD.get(lockKey);
        if (thread != null && thread != Thread.currentThread()) {
            // 不需要等待，不需要重试
            if (acquireTimeout < 1) {
                // 已经有别的线程加上锁了，不用再请求redis了 （单机版即使redis锁key自动失效了，也不用续期锁的有效期了，保证finally要移除登记记录，宕机无需考虑）
                if (isTry) {
                    return false;
                }
                throw new FrameworkException("系统繁忙,请稍后重试");
            }
        }

        // 新锁
        LockInfo newLock = LOCK_TEMPLATE.lockPlus(lockKey, expire * 1000, acquireTimeout, retryInterval, null);
        if (null == newLock) {
            if (isTry) {
                return false;
            }
            throw new FrameworkException("系统繁忙,请稍后重试...");
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
        if (!doUnLock(lockInfo, 3)) {
            throw new FrameworkException("释放锁异常");
        }
    }

    private static boolean doUnLock(LockInfo lockInfo, int retry) {
        try {
            // 释放锁
            LOCK_TEMPLATE.releaseLock(lockInfo);
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
