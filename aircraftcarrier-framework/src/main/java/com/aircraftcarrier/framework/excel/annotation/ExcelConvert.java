package com.aircraftcarrier.framework.excel.annotation;

import com.aircraftcarrier.framework.enums.IEnum;

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
public @interface ExcelConvert {

    // 需要转换的枚举
    Class<? extends IEnum> sourceEnumClass() default IEnum.class;
}
