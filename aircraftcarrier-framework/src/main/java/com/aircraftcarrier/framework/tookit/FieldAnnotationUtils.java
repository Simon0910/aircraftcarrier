package com.aircraftcarrier.framework.tookit;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class FieldAnnotationUtils {

    /**
     * 获取字段上指定类型的注解（支持元注解）
     * @param field 目标字段
     * @param annotationType 注解类型
     * @return 注解实例，不存在则返回null
     */
    public static <A extends Annotation> A getAnnotation(Field field, Class<A> annotationType) {
        return AnnotationUtils.findAnnotation(field, annotationType);
    }

    /**
     * 获取类中所有带有指定注解的字段
     * @param clazz 目标类
     * @param annotationType 注解类型
     * @return 字段列表
     */
    public static List<Field> getFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotationType) {
        List<Field> result = new ArrayList<>();
        ReflectionUtils.doWithFields(clazz, field -> {
            if (AnnotationUtils.findAnnotation(field, annotationType) != null) {
                result.add(field);
            }
        });
        return result;
    }
}