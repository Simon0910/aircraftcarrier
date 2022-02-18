package com.aircraftcarrier.framework.tookit;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lzp
 */
public class MapUtil {

    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<>(16);
    }

    public static <K, V> HashMap<K, V> newHashMap(int expectedSize) {
        return new HashMap<>(capacity(expectedSize));
    }

    public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap(int expectedSize) {
        return new ConcurrentHashMap<>(capacity(expectedSize));
    }

    private static int capacity(int expectedSize) {
        if (expectedSize < 3) {
            if (expectedSize < 0) {
                throw new IllegalArgumentException("expectedSize cannot be negative but was: " + expectedSize);
            } else {
                return expectedSize + 1;
            }
        } else {
            return expectedSize < 1073741824 ? (int) ((float) expectedSize / 0.75F + 1.0F) : 2147483647;
        }
    }
}
