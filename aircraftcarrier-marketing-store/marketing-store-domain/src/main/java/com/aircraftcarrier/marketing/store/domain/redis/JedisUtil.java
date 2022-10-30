package com.aircraftcarrier.marketing.store.domain.redis;

import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisCluster;

/**
 * @author lzp
 */
@ConditionalOnClass({JedisCluster.class})
@AutoConfigureAfter(JedisCluster.class)
@Configuration
public class JedisUtil implements ApplicationContextAware {

    private static JedisCluster jedisCluster;

    public static long expire(final String key, final long seconds) {
        return jedisCluster.expire(key, seconds);
    }

    public static String set(final String key, final String value) {
        return jedisCluster.set(key, value);
    }

    public static String get(final String key) {
        return jedisCluster.get(key);
    }

    public static long decrBy(final String key, final long decrement) {
        return jedisCluster.decrBy(key, decrement);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        jedisCluster = applicationContext.getBean(JedisCluster.class);
    }
}
