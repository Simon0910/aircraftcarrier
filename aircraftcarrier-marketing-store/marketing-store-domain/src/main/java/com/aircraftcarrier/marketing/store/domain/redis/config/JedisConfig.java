package com.aircraftcarrier.marketing.store.domain.redis.config;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * 单机版
 *
 * @author zhipengliu
 * @date 2024/7/7
 * @since 1.0
 */
@Configuration
public class JedisConfig implements DisposableBean {

    private JedisPool jedisPool;

    @Bean
    public JedisPool getJedisPool() {
        // 配置 Jedis 连接池
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10); // 最大连接数
        poolConfig.setMaxIdle(5);   // 最大空闲连接数
        poolConfig.setMinIdle(1);   // 最小空闲连接数
        // 设置开启jmx功能
        poolConfig.setJmxEnabled(false);
        // poolConfig.setJmxNamePrefix("myApp:type=JedisPool,name=myPool");
        // poolConfig.setJmxNameBase("myApp");

        // 创建 Jedis 连接池
        jedisPool = new JedisPool(poolConfig, "82.157.100.48", 6379, Protocol.DEFAULT_TIMEOUT, "123123");
        return jedisPool;
    }


    @Override
    public void destroy() throws Exception {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}
