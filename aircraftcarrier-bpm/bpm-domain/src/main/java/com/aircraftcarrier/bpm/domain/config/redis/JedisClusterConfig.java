package com.aircraftcarrier.bpm.domain.config.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Connection;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * @author lzp
 */
@ConditionalOnClass({JedisCluster.class})
@ConditionalOnMissingBean({JedisCluster.class})
@Configuration
public class JedisClusterConfig {

    @Value("${redis.cluster.nodes}")
    private String nodes;
    @Value("${redis.cluster.max-attempts:1000}")
    private int maxAttempts;
    @Value("${redis.cluster.max-total-retries-duration:5}")
    private int maxTotalRetriesDuration;

    @Value("${redis.client.user:}")
    private String user;
    @Value("${redis.client.password:}")
    private String password;
    @Value("${redis.client.timeout-millis:5000}")
    private int timeoutMillis;
    @Value("${redis.client.connection-timeout-millis:5000}")
    private int connectionTimeoutMillis;

    @Value("${redis.pool.max-total:40}")
    private int maxTotal;
    @Value("${redis.pool.max-idle:24}")
    private int maxIdle;
    @Value("${redis.pool.min-idle:2}")
    private int minIdle;
    @Value("${redis.pool.max-wait:3000}")
    private long maxWaitMillis;

    @Bean
    public JedisCluster getJedisCluster() {
        String[] cNodes = nodes.split(",");
        Set<HostAndPort> clusterNodes = new HashSet<>();
        // 分割出集群节点
        for (String node : cNodes) {
            String[] hp = node.split(":");
            clusterNodes.add(new HostAndPort(hp[0].trim(), Integer.parseInt(hp[1].trim())));
        }

        // poolConfig
        GenericObjectPoolConfig<Connection> poolConfig = new GenericObjectPoolConfig<>();
        // 设置最大连接数为默认值的5倍 GenericObjectPoolConfig.DEFAULT_MAX_TOTAL * 5
        poolConfig.setMaxTotal(maxTotal);
        // 设置最大空闲连接数为默认值的3倍
        poolConfig.setMaxIdle(maxIdle);
        // 设置最小空闲连接数为默认值的2倍
        poolConfig.setMinIdle(minIdle);
        // 设置开启jmx功能
        poolConfig.setJmxEnabled(true);
        // 设置连接池没有连接后客户端的最大等待时间(单位为毫秒)
        poolConfig.setMaxWaitMillis(maxWaitMillis);

        // clientConfig
        DefaultJedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
//                .user(user)
                .password(password)
                .timeoutMillis(timeoutMillis)
                .connectionTimeoutMillis(connectionTimeoutMillis)
                .build();

        // 创建集群对象
        return new JedisCluster(clusterNodes, clientConfig, maxAttempts, Duration.ofSeconds(maxTotalRetriesDuration), poolConfig);
    }

}
