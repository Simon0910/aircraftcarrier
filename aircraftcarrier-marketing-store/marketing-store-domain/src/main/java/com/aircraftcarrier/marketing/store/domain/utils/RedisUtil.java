package com.aircraftcarrier.marketing.store.domain.utils;

import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;

@Component
public class RedisUtil {

    @Resource
    private JedisCluster jedisCluster;


}
