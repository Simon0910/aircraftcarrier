package com.aircraftcarrier.framework.excel.annotation;

import org.apache.poi.ss.usermodel.DataValidationConstraint;

import java.lang.annotation.*;

/**
 * 数值校验
 *
 * @author zhipengliu
 * @date 2025/5/3
 * @since 1.0
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelNumber {
    int validationType() default DataValidationConstraint.ValidationType.INTEGER;

    int operatorType() default DataValidationConstraint.OperatorType.BETWEEN;

    int formula1() default Integer.MIN_VALUE;

    int formula2() default Integer.MAX_VALUE;
}
