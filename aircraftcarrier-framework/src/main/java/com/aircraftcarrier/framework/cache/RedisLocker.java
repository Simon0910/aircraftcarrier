package com.aircraftcarrier.framework.cache;

import com.baomidou.lock.LockInfo;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁
 *
 * @author lzp
 * @since 2023/06/16 17:11
 */
public class RedisLocker {
    private final String lockKey;
    private final long expire;
    private final long timeout;
    private final TimeUnit unit;

    @Getter
    private boolean locked;
    private String lockValue;
    private LockInfo lockInfo;
    private int acquireCount;

    RedisLocker(String lockKey, long expire, long timeout, TimeUnit unit, boolean locked, String lockValue, LockInfo lockInfo) {
        this.lockKey = lockKey;
        this.expire = expire;
        this.timeout = timeout;
        this.unit = unit;

        this.locked = locked;
        this.lockValue = lockValue;
        this.lockInfo = lockInfo;
    }

    String getLockKey() {
        return lockKey;
    }

    String getLockValue() {
        return lockValue;
    }

    LockInfo getLockInfo() {
        return lockInfo;
    }

    int getAcquireCount() {
        return acquireCount;
    }

    void incr() {
        acquireCount++;
    }

    void decr() {
        acquireCount--;
    }

    public boolean lock() {
        if (isLocked()) {
            // reentrant
            return LockUtil2.reentrantLock(this);
        }

        RedisLocker locker = LockUtil2.tryLock(lockKey, expire, timeout, unit);
        if (!locker.isLocked()) {
            this.locked = false;
            return false;
        }
        this.locked = true;
        this.lockValue = locker.getLockValue();
        this.lockInfo = locker.getLockInfo();
        return true;
    }

    public void unLock() {
        LockUtil2.unLock(this);
    }
}
