package com.aircraftcarrier.marketing.store.adapter.common;

import com.aircraftcarrier.framework.model.response.MultiResponse;
import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.framework.support.context.EnumMappingContext;
import com.aircraftcarrier.marketing.store.adapter.ApiSortConstant;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 枚举列表
 *
 * @author lzp
 */
@ApiSort(ApiSortConstant.ENUM_CONTROLLER)
@Api(tags = "EnumController", produces = "application/json")
@Slf4j
@RequestMapping(value = "/enums/")
@RestController
public class EnumController {

    @ApiOperationSupport(order = 10)
    @ApiOperation("获取枚举列表")
    @GetMapping("/getEnumList")
    public SingleResponse<Map<String, List<Map<String, Object>>>> getEnumList() {
        return SingleResponse.ok(EnumMappingContext.getEnumList());
    }


    @ApiOperationSupport(order = 20)
    @ApiOperation("根据类名获取枚举")
    @GetMapping("/getEnumByClassName/{className}")
    public MultiResponse<Map<String, Object>> getEnumByClassName(@PathVariable("className") String className) {
        return MultiResponse.ok(EnumMappingContext.getEnumByClassName(className));
    }
}
