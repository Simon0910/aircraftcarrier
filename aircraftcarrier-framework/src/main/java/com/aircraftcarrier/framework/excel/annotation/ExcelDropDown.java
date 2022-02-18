package com.aircraftcarrier.framework.excel.annotation;

import com.aircraftcarrier.framework.enums.IEnum;
import com.aircraftcarrier.framework.excel.handler.DropDownInterface;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记导出excel的下拉数据集
 *
 * @author lzp
 */
@Documented
// 作用在字段上
@Target(ElementType.FIELD)
// 运行时有效
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelDropDown {

    // 如果没有index, 使用字段的下标
    int index() default -1;

    // 固定下拉内容
    String[] source() default {};

    // 枚举类下拉内容
    Class<? extends IEnum> sourceEnumClass() default IEnum.class;

    // 动态下拉内容
    Class<? extends DropDownInterface> sourceClass() default DropDownInterface.class;

}