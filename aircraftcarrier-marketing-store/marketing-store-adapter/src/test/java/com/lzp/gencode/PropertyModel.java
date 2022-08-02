package com.lzp.gencode;

import lombok.Getter;
import lombok.Setter;

/**
 * 类注释内容
 *
 * @author zhipengliu
 * @date 2022/8/1
 * @since 1.0
 */
@Getter
@Setter
public class PropertyModel {

    /**
     * String ｜ Number ｜ java.lang.List
     */
    private String propertyType;

    /**
     * eg：age
     */
    private String propertyName;

    /**
     * 说明
     */
    private String comment;

    /**
     * 示例
     */
    private Object example;

    /**
     * 泛型T
     * java.lang.List<T>
     */
    private String generic;

    /**
     * 泛型T
     * java.lang.Map<K, V>
     */
    private String mapGenericKey;
    private String mapGenericValue;
    private boolean required;
}
