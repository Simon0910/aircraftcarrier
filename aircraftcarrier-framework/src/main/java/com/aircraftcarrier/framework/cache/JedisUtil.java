package com.aircraftcarrier.framework.cache;

import com.aircraftcarrier.framework.tookit.ApplicationContextUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

/**
 * @author lzp
 */
public class JedisUtil {

    private static JedisCluster jedisCluster;

    private static JedisPool jedisPool;

    private JedisUtil() {
    }

    public static long expire(final String key, final long seconds) {
        return getJedisCluster().expire(key, seconds);
    }

    public static String set(final String key, final String value) {
        return getJedisCluster().set(key, value);
    }

    public static String get(final String key) {
        return getJedisCluster().get(key);
    }

    public static long decrBy(final String key, final long decrement) {
        return getJedisCluster().decrBy(key, decrement);
    }

    public static void del(final String key) {
        getJedisCluster().del(key);
    }

    private static Jedis getJedisCluster() {
        // Cluster
        // if (jedisCluster == null) {
        //     jedisCluster = ApplicationContextUtil.getBean(JedisCluster.class);
        // }

        // 单机版
        if (jedisPool == null) {
            jedisPool = ApplicationContextUtil.getBean(JedisPool.class);
        }
        return jedisPool.getResource();
    }
}
