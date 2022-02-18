package com.aircraftcarrier.framework.cache.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * cache 配置文件。
 *
 * @author panqingcui
 * @date 2017-10-24 11:24
 */
@EnableCaching
@Configuration
public class CacheAutoConfiguration {
    public static final int DEFAULT_MAXSIZE = 50000;
    public static final int DEFAULT_TTL = 24;

    /**
     * 创建基于Caffeine的Cache Manager
     *
     * @return CacheManager
     */
    @Bean
    public CacheManager caffeineCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        ArrayList<CaffeineCache> caches = new ArrayList<>();
        for (Caches c : Caches.values()) {
            caches.add(new CaffeineCache(c.name(),
                    Caffeine.newBuilder().recordStats()
                            .expireAfterWrite(c.getTtl(), TimeUnit.HOURS)
                            .maximumSize(c.getMaxSize())
                            .build()));
        }
        cacheManager.setCaches(caches);
        return cacheManager;
    }

    /**
     * 创建缓存，有效期，容量
     */
    public enum Caches {
        // 默认 24小时 5W
        DEFAULT,
        // 1小时，最大容量1000
        CUSTOM(1, 1000),
        ;

        /**
         * 最大數量
         */
        private int maxSize = DEFAULT_MAXSIZE;
        /**
         * 过期时间（小时）
         */
        private int ttl = DEFAULT_TTL;

        Caches() {
        }

        Caches(int ttl) {
            this.ttl = ttl;
        }

        Caches(int ttl, int maxSize) {
            this.ttl = ttl;
            this.maxSize = maxSize;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public int getTtl() {
            return ttl;
        }
    }
}
