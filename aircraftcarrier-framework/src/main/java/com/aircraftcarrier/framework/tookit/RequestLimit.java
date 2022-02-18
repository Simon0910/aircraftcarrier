package com.aircraftcarrier.framework.tookit;

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
public final class RequestLimit {
    /**
     * 最大资源上限
     */
    private static final Integer UPPER_LIMIT = 1;

    /**
     * 资源池
     */
    private static final Map<String, Integer> LIMIT = MapUtil.newHashMap();

    /**
     * 私有
     */
    private RequestLimit() {
    }

    /**
     * jvm的机制去保证多线程并发安全
     * <p>
     * 内部类的初始化，一定只会发生一次，不管多少个线程并发去初始化
     *
     * @return
     */
    public static RequestLimit getInstance() {
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

        synchronized (this) {
            sum = LIMIT.get(key);
            if (sum == null || sum < 0) {
                LIMIT.put(key, 1);
                return true;
            }
            if (sum >= upperLimit) {
                return false;
            }
            LIMIT.put(key, ++sum);
        }

        return true;
    }

    /**
     * 释放资源
     */
    public synchronized void release(String key) {
        Integer sum = LIMIT.get(key);
        if (sum == null || sum < 0) {
            LIMIT.put(key, 0);
        } else {
            LIMIT.put(key, --sum);
        }
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
        private static final RequestLimit INSTANCE;

        static {
            INSTANCE = new RequestLimit();
        }

        /**
         * getInstance
         *
         * @return RequestLimit
         */
        public static RequestLimit getInstance() {
            return INSTANCE;
        }
    }
}