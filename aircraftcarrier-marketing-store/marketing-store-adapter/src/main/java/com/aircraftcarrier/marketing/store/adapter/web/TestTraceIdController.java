package com.aircraftcarrier.marketing.store.adapter.web;

import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.framework.support.trace.TraceIdUtil;
import com.aircraftcarrier.framework.tookit.LogUtil;
import com.aircraftcarrier.framework.web.client.ApiException;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试
 *
 * @author lzp
 */
@Api(tags = "TestTraceIdController", produces = "application/json")
@Slf4j
@RequestMapping(value = "/web/test/trace")
@RestController
public class TestTraceIdController {

    private int i = 1;

    @ApiOperationSupport(order = 1)
    @ApiOperation("hello")
    @GetMapping("/hello")
    public SingleResponse<String> hello() {
        LogUtil.setTraceFixedName("orderNo");
        LogUtil.setTraceModuleName("校验模块");
        LogUtil.requestStart(Long.parseLong(TraceIdUtil.getTraceId()), "orderNo1", "校验模块1");
        try {
            log.info(LogUtil.getLog("hello trace start"));

            Map<String, Object> orderInfo = new HashMap<>();
            orderInfo.put("id", 123);
            orderInfo.put("name", null);
            log.info(LogUtil.getLogAutoJson("如参校验 orderInfo：{}", orderInfo));

            Map<String, Object> nullObj = null;
            log.info(LogUtil.getLogAutoJson("如参校验 nullObj：{}", nullObj));

            Map<String, Object> emptyObj = new HashMap<>();
            log.info(LogUtil.getLogAutoJson("如参校验 emptyObj：{}", emptyObj));

            if (i % 3 == 0) {
                throw new ApiException("orderNo must not be null!");
            }

            log.info(LogUtil.getLog("hello trace end"));
        } catch (Exception e) {
            log.error(LogUtil.getLog("请求查询订单接口异常"), e);
        } finally {
            LogUtil.setTraceFixedName(null);
            LogUtil.setTraceModuleName(null);
            log.info(LogUtil.getLog("hello trace return"));

            LogUtil.requestEnd();
            i++;
        }
        return SingleResponse.ok("hello Trace");
    }


}