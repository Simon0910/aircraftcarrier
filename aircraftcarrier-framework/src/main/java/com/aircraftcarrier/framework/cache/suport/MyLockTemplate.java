package com.aircraftcarrier.framework.cache.suport;

import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.executor.LockExecutor;
import com.baomidou.lock.spring.boot.autoconfigure.Lock4jProperties;
import com.baomidou.lock.util.LockUtil;
import lombok.extern.slf4j.Slf4j;

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
     * @param retryInterval  每次间隔时间(ms)
     * @param executor       执行器
     * @return 加锁成功返回锁信息 失败返回null
     */
    public LockInfo lockPlus(String key, long expire, long acquireTimeout, long retryInterval, Class<? extends LockExecutor> executor) {
        expire = expire == 0 ? properties.getExpire() : expire;
        // 防止无限制重试，固定重试3次，eg：等待3秒，每次睡眠1毫秒，count = 3000 / 1 = 3000次
        // 正常情况需要改造源码： 需要配合等待时间acquireTimeout，通过参数动态传递过来retryInterval
        LockExecutor<?> lockExecutor = obtainExecutor(executor);
        log.debug(String.format("use lock class: %s", lockExecutor.getClass()));
        String value = LockUtil.simpleUUID();
        Object lockInstance = lockExecutor.acquire(key, value, expire, acquireTimeout);
        if (null != lockInstance) {
            log.info("locked key [{}]: Thread: {}", key, Thread.currentThread().getName());
            return new LockInfo(key, value, expire, acquireTimeout, 0, lockInstance, lockExecutor);
        }
        return null;
    }

}
