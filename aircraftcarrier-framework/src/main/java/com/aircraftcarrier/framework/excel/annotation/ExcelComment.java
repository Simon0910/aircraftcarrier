package com.aircraftcarrier.framework.excel.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author lzp
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelComment {

    // 如果没有index, 使用字段的下标
    int index() default -1;

    int row() default 0;

    int width() default 3;

    int height() default 5;

    String comment() default "";

}
