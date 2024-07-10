package com.aircraftcarrier.framework.tookit;

import com.aircraftcarrier.framework.tookit.lock.LockKeyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RequestLimit
 *
 * @author lzp
 * @version 1.0
 * @date 2020-06-03
 */
public final class RequestLimitUtil {
    /**
     * 最大资源上限
     */
    private static final Integer UPPER_LIMIT = 1;

    /**
     * 资源池
     * 预估好同时申请资源的key的种类数量，防止因为map扩容缩容造成卡顿现象 （极端现象同时一万个不同的key来申请，会扩容，移除后缩容）
     * { @link https://developer.aliyun.com/article/776568 }
     */
    private static final Map<String, Integer> LIMIT = MapUtil.newConcurrentHashMap(1024);

    /**
     * 私有
     */
    private RequestLimitUtil() {
    }

    /**
     * jvm的机制去保证多线程并发安全
     * <p>
     * 内部类的初始化，一定只会发生一次，不管多少个线程并发去初始化
     *
     * @return RequestLimitUtil
     */
    public static RequestLimitUtil getInstance() {
        return Singleton.getInstance();
    }

    public static int getKeyNum(String key) {
        return LIMIT.getOrDefault(key, 0);
    }

    /**
     * 申请资源
     */
    public boolean require(String key) {
        return require(key, UPPER_LIMIT);
    }

    /**
     * 申请资源
     */
    public boolean require(String key, Integer upperLimit) {
        Integer sum = LIMIT.get(key);
        if (sum != null && sum >= upperLimit) {
            return false;
        }

        LockKeyUtil.lock(key);
        try {
            sum = LIMIT.get(key);
            if (sum == null || sum < 0) {
                LIMIT.put(key, 1);
                return true;
            }
            if (sum >= upperLimit) {
                return false;
            }
            LIMIT.put(key, ++sum);
        } finally {
            LockKeyUtil.unlock(key);
        }
        return true;
    }

    /**
     * 释放资源
     */
    public void release(String key) {
        LockKeyUtil.lock(key);
        Integer sum = LIMIT.get(key);
        if (sum == null || sum < 0) {
            LIMIT.put(key, 0);
        } else {
            LIMIT.put(key, --sum);
        }
        LockKeyUtil.unlock(key);
    }

    public List<String> getKeys() {
        return new ArrayList<>(LIMIT.keySet());
    }

    /**
     * 静态内部类的方式, 初始化单例
     */
    private static class Singleton {
        /**
         * 实例
         */
        private static final RequestLimitUtil INSTANCE;

        static {
            INSTANCE = new RequestLimitUtil();
        }

        /**
         * getInstance
         *
         * @return RequestLimit
         */
        public static RequestLimitUtil getInstance() {
            return INSTANCE;
        }
    }
}