package com.aircraftcarrier.marketing.store.adapter.web;

import com.aircraftcarrier.framework.concurrent.TraceRunnable;
import com.aircraftcarrier.framework.concurrent.TraceThreadPoolExecutor;
import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.framework.security.core.LoginUser;
import com.aircraftcarrier.framework.security.core.LoginUserUtil;
import com.aircraftcarrier.framework.tookit.Log;
import com.aircraftcarrier.marketing.store.client.TestService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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
    private final TraceThreadPoolExecutor threadPoolExecutor = new TraceThreadPoolExecutor(1, 1, 3000, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    static Object nullObject = null;
    static HashMap<Object, Object> emptyObject = new HashMap<>();
    static HashMap<Object, Object> orderInfo = new HashMap<>();

    static {
        orderInfo.put("id", "1");
        orderInfo.put("orderNo", "123");
        orderInfo.put("orderInfoDetail", emptyObject);
        orderInfo.put("isNull", nullObject);
    }

    @Resource
    private TestService testService;
    private int i = 1;


    @ApiOperationSupport(order = 1)
    @ApiOperation("hello log")
    @GetMapping("/helloLog")
    public SingleResponse<String> helloLog() {
        Log.setTraceFixedName("orderNo");
        Log.setTraceModuleName("校验模块");

        Log.requestStart("订单号", "模块1");
        try {
            log.info(Log.getInfoLog("入参: 【{}】", Log.toJsonStringInfo(orderInfo)));
            log.info(Log.getInfoLog("出参: 【{}】", "orderNo"));

            Log.resetModule("模块2");

            log.info(Log.getInfoLog("入参: 【{}】", Log.toJsonStringInfo(orderInfo)));
            log.info(Log.getInfoLog("出参: 【{}】", "orderNo"));

            if (i % 5 == 0) {
                throw new RuntimeException("错误了！");
            }
        } catch (Exception e) {
            log.error(Log.getErrorLog("helloLog接口异常"), e);
        } finally {
            Log.requestEnd();
            i++;
        }

        return SingleResponse.ok("hello log");
    }


    @ApiOperationSupport(order = 2)
    @ApiOperation("hello log2")
    @GetMapping("/helloLog2")
    public SingleResponse<String> helloLog2() {
        Log.setTraceFixedName("orderNo");
        Log.setTraceModuleName("校验模块");

        Log.requestStart("订单号", "模块1");
        try {
            Log.info("1入参数：{}", Log.toJsonSupplier(orderInfo));

            Log.infoToJson("2入参数：{}", orderInfo);

            if (i % 5 == 0) {
                throw new RuntimeException("错误了！");
            }
        } catch (Exception e) {
            Log.error("helloLog2接口异常", e);
            // Log.error("helloLog2接口异常 {} {}", Log.getSupplier(11), Log.getSupplier(11), Log.getSupplier(e));
        } finally {
            Log.requestEnd();
            i++;
        }
        return SingleResponse.ok("hello log2");
    }


    @ApiOperationSupport(order = 3)
    @ApiOperation("事件发布")
    @GetMapping("/publishEvent")
    public SingleResponse<Void> publishEvent() {
        Log.setTraceFixedName("orderNo");
        Log.setTraceModuleName("校验模块");

        Log.requestStart("订单号", "main线程");

        LoginUser loginUser = LoginUserUtil.getLoginUser();
        Log.info("start...");
        Log.info("Main LoginUser：{}", Log.toJsonSupplier(loginUser));

        new Thread(new TraceRunnable(() -> {
            Log.resetFixAndModule("订单号", "线程1");
            Log.info("start1");
            LoginUser loginUser1 = LoginUserUtil.getLoginUser();
            Log.info("end1 loginUser1：{}", Log.toJsonSupplier(loginUser1));
        })).start();

        testService.publishEvent();

        threadPoolExecutor.execute(() -> {
            Log.resetFixAndModule("订单号", "线程2");
            Log.info("start2");
            LoginUser loginUser2 = LoginUserUtil.getLoginUser();
            Log.info("end2 loginUser2：{}", Log.toJsonSupplier(loginUser2));
        });

        threadPoolExecutor.execute(() -> {
            Log.resetFixAndModule("订单号", "线程3");
            Log.info("start3");
            LoginUser loginUser3 = LoginUserUtil.getLoginUser();
            Log.info("end3 loginUser3：{}", Log.toJsonSupplier(loginUser3));

            new Thread(new TraceRunnable(() -> {
                Log.resetFixAndModule("订单号", "线程4");
                Log.info("start4");
                LoginUser loginUser4 = LoginUserUtil.getLoginUser();
                Log.info("end4 loginUser4：{}", Log.toJsonSupplier(loginUser4));

                new Thread(new TraceRunnable(() -> {
                    Log.resetFixAndModule("订单号", "线程5");
                    Log.info("start5");
                    LoginUser loginUser5 = LoginUserUtil.getLoginUser();
                    Log.info("end5 loginUser5：{}", Log.toJsonSupplier(loginUser5));
                })).start();

            })).start();
        });

        return SingleResponse.ok(null);
    }


}