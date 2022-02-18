package com.aircraftcarrier.framework.tookit;

import com.aircraftcarrier.framework.exception.ErrorCode;
import com.aircraftcarrier.framework.exception.ToolException;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * BeanUtils
 *
 * @author lishudong
 * @version 1.0
 * @date 2019-06-18
 */
public class BeanUtilsBySpring {

    private BeanUtilsBySpring() {

    }

    /**
     * 拷贝属性，生成新对象
     *
     * @param orig   orig
     * @param target target
     * @param <T>    <T>
     * @return T
     */
    public static <T> T convert(Object orig, Class<T> target) {
        if (orig == null) {
            return null;
        }

        T destEntry;
        try {
            destEntry = target.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(orig, destEntry, target);
        } catch (Exception e) {
            throw new ToolException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
        return destEntry;
    }

    /**
     * 对list中对象进行属性拷贝，生成新list
     *
     * @param orig   orig
     * @param target target
     * @param <T>    <T>
     * @return List<T>
     */
    public static <T> List<T> convertList(List<?> orig, Class<T> target) {
        if (orig == null || orig.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<T> dest = new ArrayList<>(orig.size());
        try {
            Constructor<T> constructor = target.getDeclaredConstructor();
            for (Object each : orig) {
                if (each == null) {
                    dest.add(null);
                    continue;
                }
                T destEntry = constructor.newInstance();
                BeanUtils.copyProperties(each, destEntry, target);
                dest.add(destEntry);
            }
        } catch (Exception e) {
            throw new ToolException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
        return dest;
    }

}
