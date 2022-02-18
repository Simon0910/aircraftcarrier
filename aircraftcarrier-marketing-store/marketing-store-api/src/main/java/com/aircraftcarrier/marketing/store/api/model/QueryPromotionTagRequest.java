package com.aircraftcarrier.marketing.store.api.model;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author liuzhipeng
 */
@Getter
@Setter
public class QueryPromotionTagRequest implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;
    /**
     * requestId: Unique identifier for the request.
     */
    private String requestId;
    /**
     * 支付渠道
     */
    private String paymentChannel;
    /**
     * 航班编号
     */
    private String flightNumber;
}
