package com.aircraftcarrier.framework.enums;

import java.io.Serializable;

/**
 * @author lzp
 */
public interface IEnum<K extends Serializable> {

    /**
     * 数据库保存的值
     *
     * @return Object
     */
    K code();

    /**
     * 获取页面展示值
     *
     * @return String
     */
    String desc();

}
