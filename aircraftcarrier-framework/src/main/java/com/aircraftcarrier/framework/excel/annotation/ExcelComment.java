package com.aircraftcarrier.framework.excel.annotation;

import java.lang.annotation.*;

/**
 * @author lzp
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelComment {
    int width() default 2;

    int height() default 2;

    String comment() default "";

}
