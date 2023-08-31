package com.aircraftcarrier.marketing.store.adapter.web;

import com.aircraftcarrier.framework.concurrent.TraceRunnable;
import com.aircraftcarrier.framework.concurrent.TraceThreadPoolExecutor;
import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.framework.security.core.LoginUser;
import com.aircraftcarrier.framework.security.core.LoginUserUtil;
import com.aircraftcarrier.framework.tookit.LogUtil;
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
        LogUtil.setTraceFixedName("orderNo");
        LogUtil.setTraceModuleName("校验模块");

        LogUtil.requestStart("订单号", "模块1");
        try {
            log.info(LogUtil.getInfoLog("入参: 【{}】", LogUtil.toJsonStringInfo(orderInfo)));
            log.info(LogUtil.getInfoLog("出参: 【{}】", "orderNo"));

            LogUtil.resetModule("模块2");

            log.info(LogUtil.getInfoLog("入参: 【{}】", LogUtil.toJsonStringInfo(orderInfo)));
            log.info(LogUtil.getInfoLog("出参: 【{}】", "orderNo"));

            if (i % 5 == 0) {
                throw new RuntimeException("错误了！");
            }
        } catch (Exception e) {
            log.error(LogUtil.getErrorLog("helloLog接口异常"), e);
        } finally {
            LogUtil.requestEnd();
            i++;
        }

        return SingleResponse.ok("hello log");
    }


    @ApiOperationSupport(order = 2)
    @ApiOperation("hello log2")
    @GetMapping("/helloLog2")
    public SingleResponse<String> helloLog2() {
        LogUtil.setTraceFixedName("orderNo");
        LogUtil.setTraceModuleName("校验模块");

        LogUtil.requestStart("订单号", "模块1");
        try {
            LogUtil.info("1入参数：{}", LogUtil.getJsonSupplier(orderInfo));

            LogUtil.infoToJson("2入参数：{}", orderInfo);

            if (i % 5 == 0) {
                throw new RuntimeException("错误了！");
            }
        } catch (Exception e) {
            log.error(LogUtil.getInfoLog("helloLog2接口异常"), e);
        } finally {
            LogUtil.requestEnd();
            i++;
        }
        return SingleResponse.ok("hello log2");
    }


    @ApiOperationSupport(order = 3)
    @ApiOperation("事件发布")
    @GetMapping("/publishEvent")
    public SingleResponse<Void> publishEvent() {
        LogUtil.setTraceFixedName("orderNo");
        LogUtil.setTraceModuleName("校验模块");

        LogUtil.requestStart("订单号", "main线程");

        LoginUser loginUser = LoginUserUtil.getLoginUser();
        LogUtil.info("start...");
        LogUtil.info("Main LoginUser：{}", LogUtil.getJsonSupplier(loginUser));

        new Thread(new TraceRunnable(() -> {
            LogUtil.resetFixAndModule("订单号", "线程1");
            LogUtil.info("start1");
            LoginUser loginUser1 = LoginUserUtil.getLoginUser();
            LogUtil.info("end1 loginUser1：{}", LogUtil.getJsonSupplier(loginUser1));
        })).start();

        testService.publishEvent();

        threadPoolExecutor.execute(() -> {
            LogUtil.resetFixAndModule("订单号", "线程2");
            LogUtil.info("start2");
            LoginUser loginUser2 = LoginUserUtil.getLoginUser();
            LogUtil.info("end2 loginUser2：{}", LogUtil.getJsonSupplier(loginUser2));
        });

        threadPoolExecutor.execute(() -> {
            LogUtil.resetFixAndModule("订单号", "线程3");
            LogUtil.info("start3");
            LoginUser loginUser3 = LoginUserUtil.getLoginUser();
            LogUtil.info("end3 loginUser3：{}", LogUtil.getJsonSupplier(loginUser3));

            new Thread(new TraceRunnable(() -> {
                LogUtil.resetFixAndModule("订单号", "线程4");
                LogUtil.info("start4");
                LoginUser loginUser4 = LoginUserUtil.getLoginUser();
                LogUtil.info("end4 loginUser4：{}", LogUtil.getJsonSupplier(loginUser4));

                new Thread(new TraceRunnable(() -> {
                    LogUtil.resetFixAndModule("订单号", "线程5");
                    LogUtil.info("start5");
                    LoginUser loginUser5 = LoginUserUtil.getLoginUser();
                    LogUtil.info("end5 loginUser5：{}", LogUtil.getJsonSupplier(loginUser5));
                })).start();

            })).start();
        });

        return SingleResponse.ok(null);
    }


}