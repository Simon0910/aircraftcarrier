package com.aircraftcarrier.framework.tookit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * @author lzp
 */
public class CollectionUtil {

    private CollectionUtil() {
    }

    public static boolean isEmpty(Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> coll) {
        return !isEmpty(coll);
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static <K, V> V computeIfAbsent(Map<K, V> concurrentHashMap, K key, Function<? super K, ? extends V> mappingFunction) {
        V v = concurrentHashMap.get(key);
        return v != null ? v : concurrentHashMap.computeIfAbsent(key, mappingFunction);
    }

    /**
     * 根据属性去重
     *
     * @param list         list
     * @param keyExtractor keyExtractor
     * @param <T>          T
     * @param <U>          U
     * @return List<T> List<T>
     */
    public static <T, U extends Comparable<? super U>> List<T> distinct(List<T> list, Function<? super T, ? extends U> keyExtractor) {
        return list.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(keyExtractor))), ArrayList::new));
    }

    /**
     * 获取某个属性做大的对象
     *
     * @param list         list
     * @param keyExtractor keyExtractor
     * @param <T>          T
     * @param <U>          U
     * @return T T
     */
    public static <T, U extends Comparable<? super U>> T maxBy(List<T> list, Function<? super T, ? extends U> keyExtractor) {
        Optional<T> maxOne = list.stream().collect(Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparing(keyExtractor)), Function.identity()));
        return maxOne.orElse(null);
    }

    /**
     * 获取平均值
     *
     * @param list   list
     * @param mapper mapper
     * @param <T>    T
     * @return Double
     */
    public static <T> Double avgDouble(List<T> list, ToDoubleFunction<? super T> mapper) {
        // 计算年龄平均值
        return list.stream().collect(Collectors.collectingAndThen(Collectors.averagingDouble(mapper), Double::doubleValue));
    }

    /**
     * 深拷贝
     * 防止改变参数原来结构
     *
     * @param srcList src
     * @param <T>     对象
     * @return dest
     */
    public static <T> List<T> depCopy(List<T> srcList) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(srcList);
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (List<T>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


}
