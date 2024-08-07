package com.aircraftcarrier.framework.cache;

import com.aircraftcarrier.framework.cache.suport.MyLockTemplate;
import com.aircraftcarrier.framework.exception.LockNotAcquiredException;
import com.aircraftcarrier.framework.tookit.ApplicationContextUtil;
import com.aircraftcarrier.framework.tookit.StringPool;
import com.aircraftcarrier.framework.tookit.StringUtil;
import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * LockUtil
 * <p>
 * 使用方式
 * <pre> {@code
 *
 *  RedisLocker redisLocker = LockUtil2.tryLock(lockKey, 60000, 100, TimeUnit.MILLISECONDS);
 *  if (!redisLocker.isLocked()) {
 *      log.info("请稍后重试...");
 *      return;
 *  }
 *  try {
 *      // 业务逻辑...
 *      // 可重入锁
 *      if (redisLocker.lock()) {
 *          try {
 *
 *          } finally {
 *              // 内层解锁
 *              redisLocker.unlock();
 *          }
 *      }
 *      // 业务逻辑...
 *  } finally {
 *      // 外层解锁
 *      redisLocker.unLock();
 *  }
 *
 * }</pre>
 *
 * @author lzp
 * @since 2023/06/01 17:07
 */
@Slf4j
public class LockUtil2 {
    // ms
    private static final long EXPIRE = 30000;

    private LockUtil2() {
    }

    public static void init() {
        ResourceHolder.getEnv();
        ResourceHolder.getMyLockTemplate();
    }

    private static String getEnvKey(String lockKey) {
        return ResourceHolder.getEnv() + lockKey;
    }

    public static RedisLocker tryLock(String lockKey) {
        return doTryLock(lockKey, EXPIRE, 0, TimeUnit.MILLISECONDS);
    }

    public static RedisLocker tryLock(String lockKey, long expire) {
        return doTryLock(lockKey, expire, 0, TimeUnit.MILLISECONDS);
    }

    public static RedisLocker tryLockTimeout(String lockKey, long timeout) {
        return doTryLock(lockKey, EXPIRE, timeout, TimeUnit.MILLISECONDS);
    }

    public static RedisLocker tryLock(String lockKey, long expire, long timeout, TimeUnit unit) {
        return doTryLock(lockKey, expire, timeout, unit);
    }

    public static boolean reentrantLock(RedisLocker redisLocker) {
        if (redisLocker == null) {
            return false;
        }
        if (!redisLocker.isLocked()) {
            return false;
        }
        if (redisLocker.getLockKey() == null || redisLocker.getLockValue() == null) {
            return false;
        }
        String currentLockValue = JedisUtil.get(getEnvKey(redisLocker.getLockKey()));
        if (redisLocker.getLockValue().equals(currentLockValue)) {
            redisLocker.incr();
            return true;
        }
        return false;
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
    private static RedisLocker doTryLock(String lockKey, long expire, long timeout, TimeUnit unit) {
        // 校验key
        if (StringUtils.isBlank(lockKey)) {
            log.error("lockKey is blank");
            throw new LockNotAcquiredException("lockKey is blank");
        }
        if (unit == null) {
            log.error("unit is null");
            throw new LockNotAcquiredException("unit is null");
        }

        log.info("lock [{}]", lockKey);
        LockInfo lockInfo = ResourceHolder.getMyLockTemplate().lock(getEnvKey(lockKey), unit.toMillis(expire), unit.toMillis(timeout), null);
        if (lockInfo != null) {
            log.info("lock ok [{}]", lockKey);
            return new RedisLocker(lockKey, expire, timeout, unit, true, lockInfo.getLockValue(), lockInfo);
        }
        return buildNotLockedRedisLocker(lockKey, expire, timeout, unit);
    }

    @NotNull
    private static RedisLocker buildNotLockedRedisLocker(String lockKey, long expire, long timeout, TimeUnit unit) {
        return new RedisLocker(lockKey, expire, timeout, unit, false, null, null);
    }

    public static void unLock(RedisLocker redisLocker) {
        if (redisLocker == null) {
            log.warn("redisLocker is null");
            return;
        }
        if (!redisLocker.isLocked()) {
            log.warn("redisLocker is not locked");
            return;
        }
        if (redisLocker.getAcquireCount() > 0) {
            redisLocker.decr();
            return;
        }
        unLock(redisLocker.getLockKey(), redisLocker.getLockInfo());
    }

    /**
     * unLock
     *
     * @param lockKey lockKey
     */
    private static void unLock(String lockKey, LockInfo lockInfo) {
        log.info("unLock [{}] ", lockKey);
        try {
            if (lockInfo != null) {
                boolean ok = doUnLock(lockInfo, 1);
                log.debug("unLock key [{}] {}.", lockKey, ok);
            }
        } catch (Exception e) {
            log.error("unLock error key [{}] ", lockKey, e);
        }
    }

    private static boolean doUnLock(LockInfo lockInfo, int retry) {
        try {
            // 释放锁
            ResourceHolder.getMyLockTemplate().releaseLock(lockInfo);
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

    public static void forceDelLockKey(String lockKey) {
        ResourceHolder.getMyLockTemplate().delCacheLockKey(getEnvKey(lockKey));
        JedisUtil.del(getEnvKey(lockKey));
    }

    private static class ResourceHolder {
        private static MyLockTemplate myLockTemplate; // This will be lazily initialised
        private static String env;

        private static MyLockTemplate getMyLockTemplate() {
            if (ResourceHolder.myLockTemplate == null) {
                ResourceHolder.myLockTemplate = (MyLockTemplate) ApplicationContextUtil.getBean(LockTemplate.class);
            }
            return ResourceHolder.myLockTemplate;
        }

        private static String getEnv() {
            if (ResourceHolder.env == null) {
                String applicationName = ApplicationContextUtil.getApplicationContext().getApplicationName();
                applicationName = StringUtil.isNotBlank(applicationName) ? applicationName : "default";
                String profile = ApplicationContextUtil.getApplicationContext().getEnvironment().getActiveProfiles()[0];
                profile = StringUtil.isNotBlank(profile) ? profile : "env";
                ResourceHolder.env = applicationName + StringPool.COLON + profile + StringPool.COLON;
            }
            return ResourceHolder.env;
        }
    }


}
