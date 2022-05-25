package com.aircraftcarrier.marketing.store.domain.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.*;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@Configuration
@ConditionalOnClass({JedisCluster.class})
public class JedisClusterConfig {
    @Value("${spring.redis.cluster.nodes}")
    private String clusterNodes;
    @Value("${spring.redis.cluster.user}")
    private String user;
    @Value("${spring.redis.cluster.password}")
    private String password;
    @Value("${spring.redis.cluster.timeout-millis:5000}")
    private int timeoutMillis;
    @Value("${spring.redis.cluster.connection-timeout-millis:5000}")
    private int connectionTimeoutMillis;
    @Value("${spring.redis.cluster.max-attempts:1000}")
    private int maxAttempts;
    @Value("${spring.redis.cluster.max-total-retries-duration:5}")
    private int maxTotalRetriesDuration;
    @Value("${spring.redis.pool.max-total:40}")
    private int maxTotal;
    @Value("${spring.redis.pool.max-idle:24}")
    private int maxIdle;
    @Value("${spring.redis.pool.min-idle:2}")
    private int minIdle;
    @Value("${spring.redis.pool.max-wait:3000}")
    private long maxWaitMillis;

    @Bean
    public JedisCluster getJedisCluster() {
        String[] cNodes = clusterNodes.split(",");
        Set<HostAndPort> clusterNodes = new HashSet<>();
        // 分割出集群节点
        for (String node : cNodes) {
            String[] hp = node.split(":");
            clusterNodes.add(new HostAndPort(hp[0], Integer.parseInt(hp[1])));
        }

        // poolConfig
        GenericObjectPoolConfig<Connection> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(maxTotal);// 设置最大连接数为默认值的5倍 GenericObjectPoolConfig.DEFAULT_MAX_TOTAL * 5
        poolConfig.setMaxIdle(maxIdle);  // 设置最大空闲连接数为默认值的3倍
        poolConfig.setMinIdle(minIdle);  // 设置最小空闲连接数为默认值的2倍
        poolConfig.setJmxEnabled(true);  // 设置开启jmx功能
        poolConfig.setMaxWaitMillis(maxWaitMillis);// 设置连接池没有连接后客户端的最大等待时间(单位为毫秒)

        // clientConfig
        DefaultJedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
                .user(user).password(password)
                .timeoutMillis(timeoutMillis)
                .connectionTimeoutMillis(connectionTimeoutMillis)
                .build();

        // 创建集群对象
        return new JedisCluster(clusterNodes, clientConfig, maxAttempts, Duration.ofSeconds(maxTotalRetriesDuration), poolConfig);
    }

}
