package com.aircraftcarrier.marketing.store.domain.redis;

import com.aircraftcarrier.framework.cache.RedisLockRenewal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author lzp
 */
@Slf4j
@Component("redisLockRenewal")
public class RedisLockRenewalImpl implements RedisLockRenewal {

    @Override
    public void renewalKey(Serializable key, int second) {
        String s = JedisUtil.get((String) key);
        if (s == null) {
            return;
        }
        long expire = JedisUtil.expire((String) key, second);
        log.info("expire key: {}, second: {}, expire: {}", key, second, expire == 1);
    }
}
