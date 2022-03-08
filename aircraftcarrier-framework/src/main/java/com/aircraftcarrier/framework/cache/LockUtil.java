package com.aircraftcarrier.framework.cache;

import com.aircraftcarrier.framework.exception.FrameworkException;
import com.aircraftcarrier.framework.tookit.SpringContextUtils;
import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

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
    private static final LockTemplate LOCK_TEMPLATE;
    private static final ThreadLocal<LockInfo> THREAD_LOCAL = new ThreadLocal<>();

    static {
        LOCK_TEMPLATE = SpringContextUtils.getBean(LockTemplate.class);
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
        LockInfo lock = LOCK_TEMPLATE.lock(String.valueOf(key), expire * 1000, acquireTimeout * 1000);
        if (null == lock) {
            throw new FrameworkException("系统繁忙,请稍后重试");
        }
        THREAD_LOCAL.set(lock);
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
        LockInfo lock = LOCK_TEMPLATE.lock(String.valueOf(key), expire * 1000, acquireTimeout * 1000);
        if (null == lock) {
            return false;
        }
        THREAD_LOCAL.set(lock);
        return true;
    }

    public static void unLock() {
        LockInfo lockInfo = THREAD_LOCAL.get();
        THREAD_LOCAL.remove();
        if (lockInfo == null) {
            return;
        }

        try {
            //释放锁
            LOCK_TEMPLATE.releaseLock(lockInfo);
        } catch (Exception e) {
            log.error("释放锁异常");
            e.printStackTrace();
        }
    }
}
