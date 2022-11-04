package com.aircraftcarrier.marketing.store.client;

import com.aircraftcarrier.marketing.store.client.es.EsDocumentVo;

import java.util.List;

/**
 * @author liuzhipeng
 */
public interface EsService {

    /**
     * 新增
     */
    Integer insert();

    /**
     * 查询
     */
    List<EsDocumentVo> search();
}
