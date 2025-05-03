package com.aircraftcarrier.framework.excel.annotation;

import java.lang.annotation.*;

/**
 * Excel Size
 *
 * @author zhipengliu
 * @date 2025/5/3
 * @since 1.0
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelSize {
    /**
     * @return size the element must be higher or equal to
     */
    int min() default 0;

    /**
     * @return size the element must be lower or equal to
     */
    int max() default Integer.MAX_VALUE;
}
