package com.aircraftcarrier.marketing.store.adapter.common;

import com.aircraftcarrier.framework.model.response.MultiResponse;
import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.framework.support.context.EnumMappingContext;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
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
 * @author lzp
 */
@Api(tags = "EnumController", produces = "application/json")
@Slf4j
@RequestMapping(value = "/enums/")
@RestController
public class EnumController {

    @ApiOperationSupport(order = 10)
    @ApiOperation("获取所有枚举")
    @GetMapping("/getAllEnum")
    public SingleResponse<Map<String, List<Map<String, Object>>>> getAllEnum() {
        return SingleResponse.ok(EnumMappingContext.queryAllEnums());
    }


    @ApiOperationSupport(order = 20)
    @ApiOperation("根据类名获取枚举")
    @GetMapping("/getEnumsByName/{enumName}")
    public MultiResponse<Map<String, Object>> getEnumsByName(@PathVariable("enumName") String enumName) {
        return MultiResponse.ok(EnumMappingContext.queryEnumsByName(enumName));
    }
}
