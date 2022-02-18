package com.aircraftcarrier.marketing.store.api.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author liuzhipeng
 */
@Getter
@Setter
public class PromotionTag implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;
    /**
     * 标签编号
     */
    private String tagNumber;
    /**
     * 标签名称
     */
    private String tagName;
}
