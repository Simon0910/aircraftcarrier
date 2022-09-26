package com.aircraftcarrier.marketing.store.client.product.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lzp
 */
@EqualsAndHashCode
@Data
public class InventoryRequest {
    private String orderId;
    private String userId;
    private String goodsNo;
    private Integer count;
}
