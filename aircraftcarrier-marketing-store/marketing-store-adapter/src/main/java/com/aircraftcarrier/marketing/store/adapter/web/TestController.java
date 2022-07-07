package com.aircraftcarrier.marketing.store.adapter.web;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.framework.security.core.LoginUser;
import com.aircraftcarrier.framework.security.core.LoginUserUtil;
import com.aircraftcarrier.framework.support.trace.MdcRunnableDecorator;
import com.aircraftcarrier.framework.support.trace.TraceThreadPoolExecutor;
import com.aircraftcarrier.framework.tookit.JsonUtil;
import com.aircraftcarrier.marketing.store.client.TestService;
import com.aircraftcarrier.marketing.store.client.demo.request.DemoRequest;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 测试
 *
 * @author lzp
 */
@Api(tags = "TestController", produces = "application/json")
@Slf4j
@RequestMapping(value = "/web/test")
@RestController
public class TestController {

    private final TraceThreadPoolExecutor threadPoolExecutor = new TraceThreadPoolExecutor(3, 3, 3000, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    @Value("classpath:demo.json")
    private org.springframework.core.io.Resource demoResource;


    @Resource
    private TestService testService;


    @ApiOperationSupport(order = -1)
    @ApiOperation("hello")
    @GetMapping("/hello")
    public SingleResponse<String> hello() {
        return SingleResponse.ok("hello world");
    }

    @ApiOperationSupport(order = 1)
    @ApiOperation("获取json文件")
    @GetMapping("/getJson")
    public SingleResponse<JSONArray> getJson() throws IOException {
        return SingleResponse.ok(JSONUtil.readJSONArray(demoResource.getFile(), StandardCharsets.UTF_8));
    }

    @ApiOperationSupport(order = 10)
    @ApiOperation("事件发布")
    @GetMapping("/publishEvent")
    public SingleResponse<Void> publishEvent() {
        LoginUser loginUser = LoginUserUtil.getLoginUser();
        log.info("LoginUser：{}", JsonUtil.obj2Json(loginUser));

        new Thread(new MdcRunnableDecorator(() -> {
            log.info("1-获取主线程的MDC上下文,例如traceId");
            LoginUser loginUser1 = LoginUserUtil.getLoginUser();
            log.info("1-LoginUser：{}", JsonUtil.obj2Json(loginUser1));
        })).start();

        testService.publishEvent();

        threadPoolExecutor.execute(() -> {
            log.info("2-获取主线程的MDC上下文,例如traceId");
            LoginUser loginUser2 = LoginUserUtil.getLoginUser();
            log.info("2-LoginUser：{}", JsonUtil.obj2Json(loginUser2));
        });

        return SingleResponse.ok(null);
    }

    @ApiOperationSupport(order = 15)
    @ApiOperation("事务测试")
    @PostMapping("/testTransactional")
    public SingleResponse<Void> testTransactional() {
        testService.testTransactional();
        return SingleResponse.ok(null);
    }

    @ApiOperationSupport(order = 25)
    @ApiOperation(value = "测试InEnum枚举注解")
    @PostMapping("/testInEnum")
    public SingleResponse<DemoRequest> testInEnum(@RequestBody @Valid DemoRequest demoRequest) {
        return SingleResponse.ok(demoRequest);
    }

    @ApiOperationSupport(order = 30)
    @ApiOperation(value = "锁测试")
    @GetMapping("/testLock")
    public SingleResponse<String> testLock(@RequestParam Serializable id) {
        return SingleResponse.ok(testService.testLock(id));
    }

    @ApiOperationSupport(order = 31)
    @ApiOperation(value = "锁测试JVM")
    @GetMapping("/testLockKey")
    public SingleResponse<String> testLockKey(@RequestParam Serializable id) {
        for (int i = 0; i < 100; i++) {
            testService.testLockKey(id);
        }
        return SingleResponse.ok();
    }

    @ApiOperationSupport(order = 35)
    @ApiOperation(value = "Drools规则引擎测试")
    @PostMapping("/testDrools")
    public SingleResponse<String> testDrools(@RequestBody Map<String, Object> params) {
        testService.applyDiscount(params);
        return SingleResponse.ok();
    }

    @ApiOperationSupport(order = 36)
    @ApiOperation(value = "并发扣库存防止超卖")
    @GetMapping("/deductionInventory")
    public SingleResponse<String> deductionInventory(@RequestParam Serializable goodsNo) {
        for (int i = 0; i < 10; i++) {
            testService.deductionInventory(goodsNo);
        }
        return SingleResponse.ok();
    }

    @ApiOperationSupport(order = 37)
    @ApiOperation(value = "多线程测试")
    @GetMapping("/multiThread")
    public SingleResponse<String> multiThread() {
        for (int i = 0; i < 1; i++) {
            testService.multiThread();
        }
        return SingleResponse.ok();
    }
}
