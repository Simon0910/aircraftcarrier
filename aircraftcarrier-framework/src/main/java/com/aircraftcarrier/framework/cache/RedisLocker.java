package com.aircraftcarrier.framework.cache;

import com.aircraftcarrier.framework.exception.LockNotAcquiredException;
import com.baomidou.lock.LockInfo;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁
 *
 * @author ext.liuzhipeng12
 * @since 2023/06/16 17:11
 */
public class RedisLocker {

    private final String lockKey;
    private String lockValue;
    private Boolean locked;

    private Long expire;
    private Long timeout;
    private TimeUnit unit;

    private LockInfo lockInfo;

    RedisLocker(String lockKey, String lockValue, Boolean locked, LockInfo lockInfo) {
        this.lockKey = lockKey;
        this.lockValue = lockValue;
        this.locked = locked;
        this.lockInfo = lockInfo;
    }

    public RedisLocker(String lockKey, long expire, TimeUnit unit) {
        this.lockKey = lockKey;
        this.expire = expire;
        this.unit = unit;
    }

    public RedisLocker(String lockKey, long expire, long timeout, TimeUnit unit) {
        this.lockKey = lockKey;
        this.expire = expire;
        this.timeout = timeout;
        this.unit = unit;
    }

    public String getLockKey() {
        return lockKey;
    }

    public String getLockValue() {
        return lockValue;
    }

    public boolean getLocked() {
        return locked;
    }

    public LockInfo getLockInfo() {
        return lockInfo;
    }

    public boolean lock() {
        if (locked != null) {
            if (locked) {
                this.unLock();
            }
            throw new LockNotAcquiredException("illegal lock status.");
        }

        RedisLocker locker;
        if (timeout != null) {
            locker = LockUtil2.tryLock(lockKey, expire, timeout, unit);
        } else {
            locker = LockUtil2.tryLock(lockKey, expire, unit);
        }

        if (!locker.locked) {
            this.locked = false;
            return false;
        }
        this.locked = true;
        this.lockValue = locker.getLockValue();
        return true;
    }

    public boolean isLocked() {
        return locked;
    }

    public void unLock() {
        if (locked != null && !locked) {
            return;
        }
        LockUtil2.unLock(this);
    }
}
