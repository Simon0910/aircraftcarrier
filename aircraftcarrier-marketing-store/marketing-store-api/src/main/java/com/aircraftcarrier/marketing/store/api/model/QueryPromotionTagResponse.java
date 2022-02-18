package com.aircraftcarrier.marketing.store.api.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author liuzhipeng
 */
@Getter
@Setter
public class QueryPromotionTagResponse implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;
    /**
     * responseId: Unique identifier for the response.
     */
    private String responseId;

    /**
     * 标签, 根据需求变更可扩展为对象集合
     */
    private PromotionTag promotionTag;
}
