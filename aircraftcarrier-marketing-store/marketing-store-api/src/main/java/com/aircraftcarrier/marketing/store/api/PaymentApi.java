package com.aircraftcarrier.marketing.store.api;

import com.aircraftcarrier.marketing.store.api.model.QueryPromotionTagRequest;
import com.aircraftcarrier.marketing.store.api.model.QueryPromotionTagResponse;

/**
 * @author liuzhipeng
 */
public interface PaymentApi {

    /**
     * queryPromotionTag
     *
     * @param queryPromotionTagRequest queryPromotionTagRequest
     * @return QueryPromotionTagResponse
     */
    QueryPromotionTagResponse queryPromotionTag(QueryPromotionTagRequest queryPromotionTagRequest);
}
