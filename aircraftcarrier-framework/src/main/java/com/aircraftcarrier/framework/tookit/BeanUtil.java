package com.aircraftcarrier.framework.tookit;

import com.aircraftcarrier.framework.exception.ErrorCode;
import com.aircraftcarrier.framework.exception.ToolException;
import com.aircraftcarrier.framework.tookit.cglib.CglibBeanCopier;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cglib.beans.BeanCopier;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author lzp
 */
public class BeanUtil {

    /**
     * BeanCopier 缓存
     */
    private static final Cache<String, BeanCopier> CACHE = Caffeine.newBuilder()
            // 设置最后一次写入或访问后经过固定时间过期
            .expireAfterWrite(60, TimeUnit.SECONDS)
            // 初始的缓存空间大小
            .initialCapacity(100)
            // 缓存的最大条数
            .maximumSize(300).build();


    /**
     * 私有BeanUtils
     */
    private BeanUtil() {
    }


    /**
     * 转化复制实体
     *
     * @param orig   orig
     * @param target target
     * @return T
     */
    public static <T> T convert(Object orig, Class<T> target) {
        if (orig == null) {
            return null;
        }
        try {
            final BeanCopier beanCopier = createBeanCopier(orig.getClass(), target);
            T destEntry = target.getDeclaredConstructor().newInstance();
            beanCopier.copy(orig, destEntry, null);
            return destEntry;
        } catch (Exception e) {
            throw new ToolException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    public static <T> T convert(Object orig, Class<T> target, Consumer<T> consumer) {
        if (orig == null) {
            return null;
        }
        try {
            final BeanCopier beanCopier = createBeanCopier(orig.getClass(), target);
            T destEntry = target.getDeclaredConstructor().newInstance();
            beanCopier.copy(orig, destEntry, null);
            consumer.accept(destEntry);
            return destEntry;
        } catch (Exception e) {
            throw new ToolException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * 转化复制实体LIST
     *
     * @param orig   orig
     * @param target target
     * @return java.util.List<T>
     */
    public static <T> List<T> convertList(List<?> orig, Class<T> target) {
        if (orig == null || orig.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<T> dest = new ArrayList<>(orig.size());
        try {
            final BeanCopier beanCopier = createBeanCopier(orig.get(0).getClass(), target);
            Constructor<T> constructor = target.getDeclaredConstructor();
            T destEntry;
            for (Object each : orig) {
                destEntry = constructor.newInstance();
                beanCopier.copy(each, destEntry, null);
                dest.add(destEntry);
            }
        } catch (Exception e) {
            throw new ToolException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
        return dest;
    }

    /**
     * 转化复制实体LIST
     *
     * @param orig   orig
     * @param target target
     * @return java.util.List<T>
     */
    public static <T> List<T> convertList(List<?> orig, Class<T> target, Consumer<T> consumer) {
        if (orig == null || orig.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<T> dest = new ArrayList<>(orig.size());
        try {
            final BeanCopier beanCopier = createBeanCopier(orig.get(0).getClass(), target);
            Constructor<T> constructor = target.getDeclaredConstructor();
            T destEntry;
            for (Object each : orig) {
                destEntry = constructor.newInstance();
                beanCopier.copy(each, destEntry, null);
                consumer.accept(destEntry);
                dest.add(destEntry);
            }
        } catch (Exception e) {
            throw new ToolException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
        return dest;
    }


    /**
     * copyAndParse
     *
     * @param source source
     * @param target target
     * @return List<T>
     */
    public static <T> List<T> copyAndParseList(List<?> source, Class<T> target) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<T> dest = new ArrayList<>(source.size());
        try {
            final BeanCopier beanCopier = createCglibBeanCopier(source.get(0).getClass(), target);
            Constructor<T> constructor = target.getDeclaredConstructor();
            T destEntry;
            for (Object each : source) {
                destEntry = constructor.newInstance();
                beanCopier.copy(each, destEntry, null);
                dest.add(destEntry);
            }
        } catch (Exception e) {
            throw new ToolException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
        return dest;
    }


    /**
     * copyAndParse
     *
     * @param source source
     * @param target target
     * @return T
     */
    public static <T> T copyAndParse(Object source, Class<T> target) {
        T dest;
        try {
            final BeanCopier copier = createCglibBeanCopier(source.getClass(), target);
            dest = target.getDeclaredConstructor().newInstance();
            copier.copy(source, dest, null);
        } catch (Exception e) {
            throw new ToolException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
        return dest;
    }


    /**
     * createBeanCopier
     *
     * @param source source
     * @param target target
     * @return org.springframework.cglib.beans.BeanCopier
     */
    private static <T> BeanCopier createCglibBeanCopier(Class<?> source, Class<T> target) {
        String key = genKey(source, target) + "$";
        BeanCopier beanCopier = CACHE.getIfPresent(key);
        if (beanCopier == null) {
            beanCopier = CglibBeanCopier.create(source, target, false);
            CACHE.put(key, beanCopier);
        }
        return beanCopier;
    }


    /**
     * createBeanCopier
     *
     * @param source source
     * @param target target
     * @return org.springframework.cglib.beans.BeanCopier
     */
    private static <T> BeanCopier createBeanCopier(Class<?> source, Class<T> target) {
        String key = genKey(source, target);
        BeanCopier beanCopier = CACHE.getIfPresent(key);
        if (beanCopier == null) {
            beanCopier = BeanCopier.create(source, target, false);
            CACHE.put(key, beanCopier);
        }
        return beanCopier;
    }


    /**
     * 生成key
     *
     * @param source source
     * @param target target
     * @return java.lang.String
     */
    private static String genKey(Class<?> source, Class<?> target) {
        return source.getName() + target.getName();
    }
}
