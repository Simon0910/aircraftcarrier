package com.aircraftcarrier.marketing.store.adapter.web;

import com.aircraftcarrier.framework.concurrent.TraceRunnable;
import com.aircraftcarrier.framework.concurrent.TraceThreadPoolExecutor;
import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.framework.security.core.LoginUser;
import com.aircraftcarrier.framework.security.core.LoginUserUtil;
import com.aircraftcarrier.framework.support.trace.TraceIdUtil;
import com.aircraftcarrier.framework.tookit.Log;
import com.aircraftcarrier.marketing.store.client.TestService;
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
    static Object nullObject = null;
    static HashMap<Object, Object> emptyObject = new HashMap<>();
    static HashMap<Object, Object> orderInfo = new HashMap<>();

    static {
        orderInfo.put("id", "1");
        orderInfo.put("orderNo", "123");
        orderInfo.put("orderInfoDetail", emptyObject);
        orderInfo.put("isNull", nullObject);
    }

    private final TraceThreadPoolExecutor threadPoolExecutor = new TraceThreadPoolExecutor(1, 1, 3000, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    @Resource
    private TestService testService;
    private int i = 1;


    /**
     * 考点：
     * log4j定义了8个级别的log（除去OFF和ALL，可以说分为6个级别），优先级从高到低依次为：
     * OFF、FATAL、ERROR、WARN、INFO、DEBUG、TRACE、 ALL。
     * 级别越低打得越多
     */
    @GetMapping("/")
    public String hello() {
        // 调整日志级别 观察是否打印
        log.error("error...{}", Log.toErrorJsonString(orderInfo));
        log.warn("warn...{}", Log.toWarnJsonString(orderInfo));
        log.info("info...{}", Log.toInfoJsonString(orderInfo));
        log.debug("debug...{}", Log.toDebugJsonString(orderInfo));

        Log.error(log, "error2...{}", Log.toJsonSupplier(orderInfo));
        Log.warn(log, "warn2...{}", Log.toJsonSupplier(orderInfo));
        Log.info(log, "info2...{}", Log.toJsonSupplier(orderInfo));
        Log.debug(log, "debug2...{}", Log.toJsonSupplier(orderInfo));

        Log.errorToJson(log, "error3...{}", orderInfo);
        Log.warnToJson(log, "warn3...{}", orderInfo);
        Log.infoToJson(log, "info3...{}", orderInfo);
        Log.debugToJson(log, "debug3...{}", orderInfo);

        return "hi";
    }


    @ApiOperation("hello log")
    @GetMapping("/helloLog")
    public SingleResponse<String> helloLog() {
        TraceIdUtil.setFixedName("orderNo");
        TraceIdUtil.setModuleName("校验模块");

        Log.setFixAndModule("订单号", "模块1");
        try {
            log.info(Log.getInfoLog("入参: 【{}】", Log.toInfoJsonString(orderInfo)));
            log.info(Log.getInfoLog("出参: 【{}】", "orderNo"));

            Log.setModule("模块2");

            log.info(Log.getInfoLog("入参: 【{}】", Log.toInfoJsonString(orderInfo)));
            log.info(Log.getInfoLog("出参: 【{}】", "orderNo"));

            if (i % 5 == 0) {
                throw new RuntimeException("错误了！");
            }
        } catch (Exception e) {
            log.error(Log.getErrorLog("helloLog接口异常"), e);
        } finally {
            i++;
        }

        return SingleResponse.ok("hello log");
    }


    @ApiOperation("hello log2")
    @GetMapping("/helloLog2")
    public SingleResponse<String> helloLog2() {
        TraceIdUtil.setFixedName("orderNo");
        TraceIdUtil.setModuleName("校验模块");

        Log.setFixAndModule("订单号", "模块1");
        try {
            Log.info("1入参数：{}", Log.toJsonSupplier(orderInfo));

            Log.infoToJson("2入参数：{}", orderInfo);

            if (i % 5 == 0) {
                throw new RuntimeException("错误了！");
            }
        } catch (Exception e) {
            Log.error("helloLog2接口异常1", e);
            Log.error("helloLog2接口异常2 {}, {}", Log.toJsonSupplier(orderInfo), () -> 11, () -> e);
            Log.error("helloLog2接口异常3 {}, {}", () -> Log.toJsonString(orderInfo), () -> 11, () -> e);
            Log.errorToJson("helloLog2接口异常4 {}, {}", orderInfo, 11, e);
        } finally {
            i++;
        }
        return SingleResponse.ok("hello log2");
    }


    @ApiOperation("事件发布")
    @GetMapping("/publishEvent")
    public SingleResponse<Void> publishEvent() {
        TraceIdUtil.setFixedName("orderNo");
        TraceIdUtil.setModuleName("校验模块");

        Log.start("订单号", "main线程");

        LoginUser loginUser = LoginUserUtil.getLoginUser();
        Log.info("start...");
        Log.info("Main LoginUser：{}", Log.toJsonSupplier(loginUser));

        new Thread(new TraceRunnable(() -> {
            Log.setFixAndModule("订单号", "线程1");
            Log.info("start1");
            LoginUser loginUser1 = LoginUserUtil.getLoginUser();
            Log.info("end1 loginUser1：{}", Log.toJsonSupplier(loginUser1));
        })).start();

        testService.publishEvent();

        threadPoolExecutor.execute(() -> {
            Log.setFixAndModule("订单号", "线程2");
            Log.info("start2");
            LoginUser loginUser2 = LoginUserUtil.getLoginUser();
            Log.info("end2 loginUser2：{}", Log.toJsonSupplier(loginUser2));
        });

        threadPoolExecutor.execute(() -> {
            Log.setFixAndModule("订单号", "线程3");
            Log.info("start3");
            LoginUser loginUser3 = LoginUserUtil.getLoginUser();
            Log.info("end3 loginUser3：{}", Log.toJsonSupplier(loginUser3));

            new Thread(new TraceRunnable(() -> {
                Log.setFixAndModule("订单号", "线程4");
                Log.info("start4");
                LoginUser loginUser4 = LoginUserUtil.getLoginUser();
                Log.info("end4 loginUser4：{}", Log.toJsonSupplier(loginUser4));

                new Thread(new TraceRunnable(() -> {
                    Log.setFixAndModule("订单号", "线程5");
                    Log.info("start5");
                    LoginUser loginUser5 = LoginUserUtil.getLoginUser();
                    Log.info("end5 loginUser5：{}", Log.toJsonSupplier(loginUser5));
                })).start();

            })).start();
        });

        return SingleResponse.ok(null);
    }


}