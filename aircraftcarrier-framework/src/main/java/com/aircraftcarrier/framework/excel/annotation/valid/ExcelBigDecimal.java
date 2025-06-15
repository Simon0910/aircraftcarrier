package com.aircraftcarrier.framework.excel.annotation.valid;

import java.lang.annotation.*;

/**
 * Excel BidDecimal 校验
 *
 * @author zhipengliu
 * @date 2025/5/3
 * @since 1.0
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelBigDecimal {

    String min() default "" + Integer.MIN_VALUE;

    String max() default "" + Integer.MAX_VALUE;

    int integer() default 20;

    int scale() default 0;

}
