package com.aircraftcarrier.framework.cache;

import com.aircraftcarrier.framework.cache.suport.MyLockTemplate;
import com.aircraftcarrier.framework.exception.FrameworkException;
import com.aircraftcarrier.framework.tookit.ApplicationContextUtil;
import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
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
    private static final String DEFAULT_KEY = "block";
    private static final MyLockTemplate LOCK_TEMPLATE;
    private static final ThreadLocal<LockInfo> THREAD_LOCAL = new ThreadLocal<>();

    static {
        LOCK_TEMPLATE = (MyLockTemplate) ApplicationContextUtil.getBean(LockTemplate.class);
    }

    private LockUtil() {
    }

    public static void lock() {
        lock(DEFAULT_KEY, EXPIRE, -1);
    }

    public static void lock(Serializable key) {
        lock(key, EXPIRE, -1);
    }

    public static void lock(Serializable key, long expire) {
        lock(key, expire, -1);
    }

    public static void lock(Serializable key, long expire, long acquireTimeout) {
        doLock(key, expire, acquireTimeout, false);
    }

    public static Boolean tryLock() {
        return tryLock(DEFAULT_KEY, EXPIRE, -1);
    }

    public static Boolean tryLock(Serializable key) {
        return tryLock(key, EXPIRE, -1);
    }

    public static Boolean tryLock(Serializable key, long expire) {
        return tryLock(key, expire, -1);
    }

    public static Boolean tryLock(Serializable key, long expire, long acquireTimeout) {
        return doLock(key, expire, acquireTimeout, true);
    }

    private static boolean doLock(Serializable key, long expire, long acquireTimeout, boolean isTry) {
        LockInfo lockInfo = THREAD_LOCAL.get();
        if (lockInfo != null) {
            // 可重入
            return true;
        }
        LockInfo lock = LOCK_TEMPLATE.lock(String.valueOf(key), expire * 1000, acquireTimeout * 1000);
        if (null == lock) {
            if (isTry) {
                return false;
            }
            throw new FrameworkException("系统繁忙,请稍后重试");
        }
        lock.setAcquireCount(1);
        THREAD_LOCAL.set(lock);
        return true;
    }

    public static void unLock() {
        LockInfo lockInfo = THREAD_LOCAL.get();
        if (lockInfo == null) {
            THREAD_LOCAL.remove();
            return;
        }

        int acquireCount = lockInfo.getAcquireCount();
        if (acquireCount > 1) {
            // 解锁次数还没有完成，还需要继续解锁
            lockInfo.setAcquireCount(--acquireCount);
            return;
        }

        // 最后一次解锁
        THREAD_LOCAL.remove();

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
