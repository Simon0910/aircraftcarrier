package com.aircraftcarrier.framework.cache;

import java.io.Serializable;

/**
 * @author lzp
 */
public interface RedisLockRenewal {

    /**
     * 续期key的 有效期
     * 实现逻辑为 如果有key 才自动续期
     * default方法可以被重写吗？
     *
     * @param key    redis锁key
     * @param second 秒
     */
    void renewalKey(Serializable key, int second);
}
