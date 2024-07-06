package com.aircraftcarrier.framework.cache.suport;

import com.aircraftcarrier.framework.exception.LockNotAcquiredException;
import com.aircraftcarrier.framework.tookit.LockKeyUtil;
import com.aircraftcarrier.framework.tookit.SleepUtil;
import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.executor.LockExecutor;
import com.baomidou.lock.spring.boot.autoconfigure.Lock4jProperties;
import com.baomidou.lock.util.LockUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.TimeUnit;

/**
 * @author lzp
 */
@Slf4j
public class MyLockTemplate extends LockTemplate {

    private static final @NonNull Cache<String, LockRecord> lock_record_cache = Caffeine.newBuilder()
            // size
            .maximumSize(16)
            // log
            .removalListener(((key, value, cause) -> {
                // log.debug("cache - removalListener  key: {}, 原因: {}", key, cause);
            }))
            // expire
            .expireAfter(new Expiry<String, LockRecord>() {
                @Override
                public long expireAfterCreate(@NonNull String key, @NonNull LockRecord lockRecord, long currentTime) {
                    // log.debug("cache - expireAfter expireAfterCreate");
                    return TimeUnit.MILLISECONDS.toNanos(lockRecord.getExpire());
                }

                @Override
                public long expireAfterUpdate(@NonNull String key, @NonNull LockRecord lockRecord, long currentTime, @NonNegative long currentDuration) {
                    // log.debug("cache - expireAfter expireAfterUpdate");
                    return TimeUnit.MILLISECONDS.toNanos(lockRecord.getExpire());
                }

                @Override
                public long expireAfterRead(@NonNull String key, @NonNull LockRecord lockRecord, long currentTime, @NonNegative long currentDuration) {
                    // log.debug("cache - expireAfter expireAfterRead: {}", currentDuration);
                    return currentDuration;
                }
            })
            // build
            .build();

    private final Lock4jProperties properties;

    public MyLockTemplate(Lock4jProperties properties) {
        this.properties = properties;
        super.setProperties(properties);
    }

    /**
     * 加锁方法
     *
     * @param key            锁key 同一个key只能被一个客户端持有
     * @param expire         过期时间(ms) 防止死锁
     * @param acquireTimeout 尝试获取锁超时时间(ms)
     * @param executor       执行器
     * @return 加锁成功返回锁信息 失败返回null
     */
    @Override
    public LockInfo lock(String key, long expire, long acquireTimeout, Class<? extends LockExecutor> executor) {
        acquireTimeout = acquireTimeout < 0 ? properties.getAcquireTimeout() : acquireTimeout;
        // 防止acquireTimeout过大， retryInterval过小，导致一直周期循环获取
        long retryInterval = properties.getRetryInterval() < 0 ? 1000 : properties.getRetryInterval();
        LockExecutor<?> lockExecutor = obtainExecutor(executor);
        log.debug(String.format("use lock class: %s", lockExecutor.getClass()));
        expire = !lockExecutor.renewal() && expire <= 0 ? properties.getExpire() : expire;
        int acquireCount = 0;
        String value = LockUtil.simpleUUID();
        long start = System.currentTimeMillis();

        int retryNum = 3;
        int maxFastRetryNum = 1;
        boolean lastTime = false;
        try {
            do {
                // 1次尝试取锁，maxFastRetryNum 次快速取锁，(retryNum - maxFastRetryNum - 1)次间隔重试取锁 | 后面每3秒获取一次 | 超时前获取一次
                acquireCount++;
                if (acquireCount < retryNum || acquireCount % 3 == 0 || lastTime) {
                    Object lockInstance = null;
                    // 防止某个线程执行时间长没有释放锁，很多抢锁的线程在超长超时前不必请求redis
                    LockRecord oldlockRecord = lock_record_cache.getIfPresent(key);
                    if (oldlockRecord != null && oldlockRecord.getCurrentThread() != Thread.currentThread()) {
                        log.debug("tryLock not locked [{}] from cache.", key);
                    } else {
                        if (LockKeyUtil.tryLock(key)) {
                            try {
                                log.info("tryLock [{}]...", key);
                                lockInstance = lockExecutor.acquire(key, value, expire, acquireTimeout);
                            } finally {
                                LockKeyUtil.unlock(key);
                            }
                        } else {
                            log.debug("tryLock not locked [{}] from JVM.", key);
                        }
                    }

                    if (null != lockInstance) {
                        log.debug("tryLock locked key [{}]: Thread: {}", key, Thread.currentThread().getName());
                        LockRecord lockRecord = new LockRecord();
                        lockRecord.setKey(key);
                        lockRecord.setValue(value);
                        lockRecord.setExpire(expire);
                        lock_record_cache.put(key, lockRecord);
                        return new LockInfo(key, value, expire, acquireTimeout, acquireCount, lockInstance, lockExecutor);
                    } else if (lastTime) {
                        log.debug("tryLock timeout [{}]", key);
                        return null;
                    }
                }

                if (acquireCount <= maxFastRetryNum) {
                    // 假设百分之90的场景的并发key, 过几十毫秒就可以成功获取！
                    // maxFastRetryNum次 30ms间隔
                    SleepUtil.sleepMilliseconds(30);
                } else if ((acquireTimeout - (System.currentTimeMillis() - start)) <= retryInterval) {
                    // 如果超时前剩余一个间隔时间，则超时前最后获取一次
                    lastTime = true;
                } else if (acquireCount < retryNum) {
                    // (retryNum - maxFastRetryNum - 1) 次 retryInterval间隔
                    SleepUtil.sleepMilliseconds(retryInterval);
                } else {
                    // retryNum次之后，间隔一秒，每3次间隔获取一次
                    SleepUtil.sleepMilliseconds(1000);
                }
            } while (System.currentTimeMillis() - start < acquireTimeout);
        } catch (Exception e) {
            log.error("tryLock error key [{}] ", key, e);
            throw new LockNotAcquiredException(e);
        }
        log.debug("tryLock not acquired [{}].", key);
        return null;
    }

    @Override
    public boolean releaseLock(LockInfo lockInfo) {
        if (null == lockInfo) {
            return false;
        }
        delCacheLockKey(lockInfo.getLockKey());
        return lockInfo.getLockExecutor().releaseLock(lockInfo.getLockKey(), lockInfo.getLockValue(),
                lockInfo.getLockInstance());
    }

    public void delCacheLockKey(String key) {
        lock_record_cache.invalidate(key);
    }

}
