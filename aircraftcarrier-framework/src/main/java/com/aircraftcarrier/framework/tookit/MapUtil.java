package com.aircraftcarrier.framework.tookit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lzp
 */
public class MapUtil {
    /**
     * MapUtil
     */
    private MapUtil() {
    }

    public static <K, V> Map<K, V> newHashMap() {
        return new HashMap<>(16);
    }

    public static <K, V> Map<K, V> newHashMap(int expectedSize) {
        return new HashMap<>(capacity(expectedSize));
    }

    public static <K, V> Map<K, V> newConcurrentHashMap(int expectedSize) {
        return new ConcurrentHashMap<>(capacity(expectedSize));
    }

    public static int capacity(int expectedSize) {
        return expectedSize << 1;
    }
}
