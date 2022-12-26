package com.aircraftcarrier.framework.tookit;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lzp
 */
public class MapUtil {

    static final double DEFAULT_LOAD_FACTOR = 0.75d;

    /**
     * MapUtil
     */
    private MapUtil() {
    }

    public static <K, V> Map<K, V> newHashMap() {
        return new HashMap<>(16);
    }

    public static <K, V> Map<K, V> newHashMap(int expectedSize) {
        return new HashMap<>(calculateHashMapCapacity(expectedSize));
    }

    public static <K, V> Map<K, V> newConcurrentHashMap(int expectedSize) {
        return new ConcurrentHashMap<>(calculateHashMapCapacity(expectedSize));
    }

    public static <K, V> Map<K, V> newLinkedHashMap(int expectedSize) {
        return new LinkedHashMap<>(calculateHashMapCapacity(expectedSize));
    }

    /**
     * Calculate initial capacity for HashMap based classes, from expected size and default load factor (0.75).
     *
     * @param numMappings the expected number of mappings
     * @return initial capacity for HashMap based classes.
     * @since 19
     */
    public static int calculateHashMapCapacity(int numMappings) {
        return (int) Math.ceil(numMappings / DEFAULT_LOAD_FACTOR);
    }
}
