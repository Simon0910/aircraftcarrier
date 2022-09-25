package com.aircraftcarrier.marketing.store.domain.gateway;

import com.aircraftcarrier.framework.model.response.SingleResponse;

import java.io.Serializable;

/**
 * @author lzp
 */
public interface ProductGateway {

    /**
     * 加库存
     *
     * @param goodsNo goodsNo
     * @param addNum  addNum
     * @return SingleResponse
     */
    SingleResponse<Void> addInventory(Serializable goodsNo, Integer addNum);

    /**
     * 扣减库存 1个数量
     *
     * @param goodsNo goodsNo
     * @return SingleResponse
     */
    SingleResponse<Void> deductionInventory(Serializable goodsNo);

    /**
     * 扣减库存
     *
     * @param goodsNo      goodsNo
     * @param deductionNum deductionNum
     * @return SingleResponse
     */
    SingleResponse<Void> deductionInventory(Serializable goodsNo, Integer deductionNum);
}
