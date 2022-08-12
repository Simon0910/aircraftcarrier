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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * @author lzp
 */
public class BeanUtil {

    /**
     * BeanCopier 缓存
     * expireAfterAccess 在访问之后指定多少秒过期
     * expireAfterWrite 在写入数据后指定多少秒过期
     * expireAfter 通过重写Expire接口，指定过期时间
     */
    private static final Cache<String, BeanCopier> CACHE = Caffeine.newBuilder()
            // 在访问之后指定多少秒过期
            .expireAfterAccess(60, TimeUnit.SECONDS)
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
     * @param source source
     * @param target target
     * @return T
     */
    public static <T> T convert(Object source, Class<T> target) {
        if (source == null) {
            return null;
        }
        try {
            final BeanCopier beanCopier = createBeanCopier(source.getClass(), target);
            T destEntry = target.getDeclaredConstructor().newInstance();
            beanCopier.copy(source, destEntry, null);
            return destEntry;
        } catch (Exception e) {
            throw new ToolException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * 转化复制实体
     *
     * @param source   source
     * @param target   target
     * @param consumer consumer
     * @param <T>      T
     * @return T
     */
    public static <T> T convert(Object source, Class<T> target, Consumer<T> consumer) {
        if (source == null) {
            return null;
        }
        try {
            final BeanCopier beanCopier = createBeanCopier(source.getClass(), target);
            T destEntry = target.getDeclaredConstructor().newInstance();
            beanCopier.copy(source, destEntry, null);
            consumer.accept(destEntry);
            return destEntry;
        } catch (Exception e) {
            throw new ToolException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * 转化复制实体
     *
     * @param source     source
     * @param target     target
     * @param biConsumer biConsumer
     * @param <S>        S
     * @param <T>        T
     * @return T
     */
    public static <S, T> T convert(S source, Class<T> target, BiConsumer<S, T> biConsumer) {
        if (source == null) {
            return null;
        }
        try {
            final BeanCopier beanCopier = createBeanCopier(source.getClass(), target);
            T destEntry = target.getDeclaredConstructor().newInstance();
            beanCopier.copy(source, destEntry, null);
            biConsumer.accept(source, destEntry);
            return destEntry;
        } catch (Exception e) {
            throw new ToolException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * 转化复制实体
     *
     * @param source source
     * @param target target
     * @param fun    fun
     * @param <S>    S
     * @param <T>    T
     * @return T
     */
    public static <S, T> T convert(S source, Class<T> target, BiFunction<S, T, T> fun) {
        if (source == null) {
            return null;
        }
        try {
            final BeanCopier beanCopier = createBeanCopier(source.getClass(), target);
            T destEntry = target.getDeclaredConstructor().newInstance();
            beanCopier.copy(source, destEntry, null);
            fun.apply(source, destEntry);
            return destEntry;
        } catch (Exception e) {
            throw new ToolException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * 转化复制实体LIST
     *
     * @param source source
     * @param target target
     * @return java.util.List<T>
     */
    public static <S, T> List<T> convertList(List<S> source, Class<T> target) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<T> dest = new ArrayList<>(source.size());
        try {
            final BeanCopier beanCopier = createBeanCopier(source.get(0).getClass(), target);
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
     * 转化复制实体LIST
     *
     * @param source   source
     * @param target   target
     * @param consumer consumer
     * @param <T>      T
     * @return java.util.List<T>
     */
    public static <S, T> List<T> convertList(List<S> source, Class<T> target, Consumer<T> consumer) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<T> dest = new ArrayList<>(source.size());
        try {
            final BeanCopier beanCopier = createBeanCopier(source.get(0).getClass(), target);
            Constructor<T> constructor = target.getDeclaredConstructor();
            T destEntry;
            for (Object each : source) {
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
     * 转化复制实体LIST
     *
     * @param source     source
     * @param target     target
     * @param biConsumer biConsumer
     * @param <S>        S
     * @param <T>        T
     * @return List<T>
     */
    public static <S, T> List<T> convertList(List<S> source, Class<T> target, BiConsumer<S, T> biConsumer) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<T> dest = new ArrayList<>(source.size());
        try {
            final BeanCopier beanCopier = createBeanCopier(source.get(0).getClass(), target);
            Constructor<T> constructor = target.getDeclaredConstructor();
            T destEntry;
            for (S each : source) {
                destEntry = constructor.newInstance();
                beanCopier.copy(each, destEntry, null);
                biConsumer.accept(each, destEntry);
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
     * @param source source
     * @param target target
     * @param fun    fun
     * @param <S>    S
     * @param <T>    T
     * @return List<T>
     */
    public static <S, T> List<T> convertList(List<S> source, Class<T> target, BiFunction<S, T, T> fun) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<T> dest = new ArrayList<>(source.size());
        try {
            final BeanCopier beanCopier = createBeanCopier(source.get(0).getClass(), target);
            Constructor<T> constructor = target.getDeclaredConstructor();
            T destEntry;
            for (S each : source) {
                destEntry = constructor.newInstance();
                beanCopier.copy(each, destEntry, null);
                fun.apply(each, destEntry);
                dest.add(destEntry);
            }
        } catch (Exception e) {
            throw new ToolException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
        return dest;
    }

    /**
     * convertListWithUnderline
     *
     * @param source source
     * @param target target
     * @return List<T>
     */
    public static <S, T> List<T> convertListWithUnderline(List<S> source, Class<T> target) {
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
     * convertWithUnderline
     *
     * @param source source
     * @param target target
     * @return T
     */
    public static <S, T> T convertWithUnderline(S source, Class<T> target) {
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
    private static <S, T> BeanCopier createCglibBeanCopier(Class<S> source, Class<T> target) {
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
    private static <S, T> BeanCopier createBeanCopier(Class<S> source, Class<T> target) {
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
    private static <S, T> String genKey(Class<S> source, Class<T> target) {
        return source.getName() + target.getName();
    }
}
