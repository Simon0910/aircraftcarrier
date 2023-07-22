package com.aircraftcarrier.framework.cache;

import com.aircraftcarrier.framework.cache.suport.MyLockTemplate;
import com.aircraftcarrier.framework.exception.LockNotAcquiredException;
import com.aircraftcarrier.framework.tookit.ApplicationContextUtil;
import com.aircraftcarrier.framework.tookit.SleepUtil;
import com.aircraftcarrier.framework.tookit.StringPool;
import com.aircraftcarrier.framework.tookit.StringUtil;
import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.JedisCluster;

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

    private LockUtil2() {
    }

    public static void init() {
        ResourceHolder.getEnv();
        ResourceHolder.getJedisCluster();
        ResourceHolder.getMyLockTemplate();
    }

    private static String getEnvKey(String lockKey) {
        return ResourceHolder.getEnv() + lockKey;
    }


    public static RedisLocker tryLock(String lockKey, long expire, TimeUnit unit) {
        return doTryLock(lockKey, expire, 0, unit);
    }

    public static RedisLocker tryLock(String lockKey, long expire, long timeout, TimeUnit unit) {
        return doTryLock(lockKey, expire, timeout, unit);
    }

    public static boolean lockReentrant(RedisLocker redisLocker) {
        if (redisLocker == null) {
            return false;
        }
        if (!redisLocker.isLocked()) {
            return false;
        }
        if (redisLocker.getLockKey() == null || redisLocker.getLockValue() == null) {
            return false;
        }
        String currentLockValue = ResourceHolder.getJedisCluster().get(getEnvKey(redisLocker.getLockKey()));
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

        long start = System.currentTimeMillis();
        long acquireTimeout = unit.toMillis(timeout);
        // 假设百分之90的场景的并发key, 过几十毫秒就可以成功获取！
        int retryCount = 0;
        int maxFastRetryNum = 2;
        long retryInterval = 1000;
        boolean lastTime = false;
        long lastTimeRetryInterval;
        try {
            do {
                // 1次尝试取锁，2次快速取锁，3次重试取锁 | 后面每3秒获取一次 | 超时前获取一次
                if (retryCount < 6 || retryCount % 3 == 0 || lastTime) {
                    log.info("tryLock [{}]...", lockKey);
                    LockInfo lockInfo = ResourceHolder.getMyLockTemplate().lockPlus(getEnvKey(lockKey), expire, acquireTimeout, 0, null);
                    if (lockInfo != null) {
                        return new RedisLocker(lockKey, expire, timeout, unit, true, lockInfo.getLockValue(), lockInfo);
                    } else if (lastTime) {
                        log.info("tryLock timeout [{}]", lockKey);
                        return buildNotLockedRedisLocker(lockKey, expire, timeout, unit);
                    }
                }

                if (retryCount < maxFastRetryNum) {
                    SleepUtil.sleepMilliseconds(30);
                } else {
                    lastTime = (lastTimeRetryInterval = acquireTimeout - (System.currentTimeMillis() - start)) <= retryInterval;
                    if (lastTime) {
                        SleepUtil.sleepMilliseconds(lastTimeRetryInterval - 10);
                    } else {
                        SleepUtil.sleepMilliseconds(retryInterval);
                    }
                }
                retryCount++;

            } while (System.currentTimeMillis() - start < acquireTimeout);

            log.info("tryLock timeout [{}].", lockKey);
            return buildNotLockedRedisLocker(lockKey, expire, timeout, unit);
        } catch (Exception e) {
            log.error("tryLock error key [{}] ", lockKey, e);
            throw new LockNotAcquiredException(e);
        }
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
        try {
            if (lockInfo != null) {
                log.info("unLock key [{}] ", lockKey);
                boolean ok = doUnLock(lockInfo, 1);
                log.info("unLock key [{}] {}.", lockKey, ok);
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
        ResourceHolder.getJedisCluster().del(getEnvKey(lockKey));
    }


    private static class ResourceHolder {
        private static MyLockTemplate myLockTemplate; // This will be lazily initialised
        private static String env;
        private static JedisCluster jedisCluster;

        private static MyLockTemplate getMyLockTemplate() {
            if (LockUtil2.ResourceHolder.myLockTemplate == null) {
                LockUtil2.ResourceHolder.myLockTemplate = (MyLockTemplate) ApplicationContextUtil.getBean(LockTemplate.class);
            }
            return LockUtil2.ResourceHolder.myLockTemplate;
        }

        private static String getEnv() {
            if (LockUtil2.ResourceHolder.env == null) {
                String applicationName = ApplicationContextUtil.getApplicationContext().getApplicationName();
                applicationName = StringUtil.isNotBlank(applicationName) ? applicationName : "default";
                String profile = ApplicationContextUtil.getApplicationContext().getEnvironment().getActiveProfiles()[0];
                profile = StringUtil.isNotBlank(profile) ? profile : "env";
                LockUtil2.ResourceHolder.env = applicationName + StringPool.COLON + profile + StringPool.COLON;
            }
            return LockUtil2.ResourceHolder.env;
        }

        private static JedisCluster getJedisCluster() {
            if (LockUtil2.ResourceHolder.jedisCluster == null) {
                LockUtil2.ResourceHolder.jedisCluster = ApplicationContextUtil.getBean(JedisCluster.class);
            }
            return LockUtil2.ResourceHolder.jedisCluster;
        }
    }


}
