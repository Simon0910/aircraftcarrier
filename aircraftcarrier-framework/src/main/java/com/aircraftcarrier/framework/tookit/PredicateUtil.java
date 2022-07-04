package com.aircraftcarrier.framework.tookit;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author lzp
 */
public class PredicateUtil {

    private PredicateUtil() {
    }

    /**
     * 根据表达，返回一个断言
     *
     * @param keyExtractor 表达式
     * @param total        total
     * @return 断言
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor, int total) {
        Map<Object, Boolean> seen = MapUtil.newConcurrentHashMap(total);
        // putIfAbsent方法添加键值对，如果map集合中没有该key对应的值，则直接添加，并返回null，如果已经存在对应的值，则依旧为原来的值。
        // 如果返回null表示添加数据成功(不重复)，不重复(null==null :TRUE)
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
