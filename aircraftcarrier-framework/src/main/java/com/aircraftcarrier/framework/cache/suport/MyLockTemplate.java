package com.aircraftcarrier.framework.cache.suport;

import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.exception.LockException;
import com.baomidou.lock.executor.LockExecutor;
import com.baomidou.lock.spring.boot.autoconfigure.Lock4jProperties;
import com.baomidou.lock.util.LockUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author lzp
 */
@Slf4j
public class MyLockTemplate extends LockTemplate {

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
        expire = expire == 0 ? properties.getExpire() : expire;
        acquireTimeout = acquireTimeout <= 0 ? properties.getAcquireTimeout() : acquireTimeout;
        long retryInterval = properties.getRetryInterval();
        // 防止重试时间大于超时时间
        if (retryInterval >= acquireTimeout) {
            log.warn("retryInterval more than acquireTimeout,please check your configuration");
        }
        LockExecutor<?> lockExecutor = obtainExecutor(executor);
        log.debug(String.format("use lock class: %s", lockExecutor.getClass()));
        int acquireCount = 0;
        String value = LockUtil.simpleUUID();
        long start = System.currentTimeMillis();
        try {

            do {
                acquireCount++;
                Object lockInstance = lockExecutor.acquire(key, value, expire, acquireTimeout);
                if (null != lockInstance) {
                    return new LockInfo(key, value, expire, acquireTimeout, acquireCount, lockInstance,
                            lockExecutor);
                }
                TimeUnit.MILLISECONDS.sleep(retryInterval);
            } while (System.currentTimeMillis() - start < acquireTimeout);

        } catch (InterruptedException e) {
            log.error("lock error", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            throw new LockException();
        }
        return null;
    }

}
