package com.aircraftcarrier.marketing.store.adapter.web.es;

import com.aircraftcarrier.framework.model.response.MultiResponse;
import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.marketing.store.client.EsService;
import com.aircraftcarrier.marketing.store.client.es.EsDocumentVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 测试使用Easy-ES
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 *
 * @author liuzhipeng
 */
@RestController
@RequestMapping(value = "/web/es")
public class EsController {

    // @Resource
    private EsService esService;


    @GetMapping("/insert")
    public SingleResponse<Integer> insert() {
        return SingleResponse.ok(esService.insert());
    }

    @GetMapping("/search")
    public MultiResponse<EsDocumentVo> search() {
        return MultiResponse.ok(esService.search());
    }

}