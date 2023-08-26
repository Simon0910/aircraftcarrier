package com.aircraftcarrier.marketing.store.adapter.web;

import com.aircraftcarrier.framework.concurrent.TraceRunnable;
import com.aircraftcarrier.framework.concurrent.TraceThreadPoolExecutor;
import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.framework.security.core.LoginUser;
import com.aircraftcarrier.framework.security.core.LoginUserUtil;
import com.aircraftcarrier.framework.tookit.LogUtil;
import com.aircraftcarrier.framework.web.client.ApiException;
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
import java.util.Map;
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
    @Resource
    private TestService testService;
    private int i = 1;


    @ApiOperationSupport(order = 1)
    @ApiOperation("hello trace")
    @GetMapping("/hello")
    public SingleResponse<String> hello() {
        LogUtil.setTraceFixedName("orderNo");
        LogUtil.setTraceModuleName("校验模块");

        try {
            log.info("普通日志 {}, {}", LogUtil.toJsonString(null), LogUtil.toJsonString(new HashMap<>()));

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


    @ApiOperationSupport(order = 10)
    @ApiOperation("事件发布")
    @GetMapping("/publishEvent")
    public SingleResponse<Void> publishEvent() {
        LogUtil.setTraceFixedName("orderNo");
        LogUtil.setTraceModuleName("模块1");

        LogUtil.resetFixed("orderNo");
        LogUtil.resetModule("main");

        LoginUser loginUser = LoginUserUtil.getLoginUser();
        log.info(LogUtil.getLog("start..."));
        log.info("Main LoginUser：{}", LogUtil.toJsonString(loginUser));

        new Thread(new TraceRunnable(() -> {
            LogUtil.resetModule("线程1");
            log.info(LogUtil.getLog("线程1"));
            LoginUser loginUser1 = LoginUserUtil.getLoginUser();
            log.info(LogUtil.getLog("线程1 loginUser1：{}", LogUtil.toJsonString(loginUser1)));
        })).start();

        testService.publishEvent();

        threadPoolExecutor.execute(() -> {
            LogUtil.resetModule("线程2");
            log.info(LogUtil.getLog("线程2"));
            LoginUser loginUser2 = LoginUserUtil.getLoginUser();
            log.info(LogUtil.getLog("线程3 loginUser2：{}", LogUtil.toJsonString(loginUser2)));
        });

        threadPoolExecutor.execute(() -> {
            LogUtil.resetModule("线程3");
            log.info(LogUtil.getLog("线程3"));
            LoginUser loginUser3 = LoginUserUtil.getLoginUser();
            log.info(LogUtil.getLog("线程3 loginUser3：{}", LogUtil.toJsonString(loginUser3)));

            new Thread(new TraceRunnable(() -> {
                LogUtil.resetModule("线程4");
                log.info(LogUtil.getLog("线程4"));
                LoginUser loginUser4 = LoginUserUtil.getLoginUser();
                log.info(LogUtil.getLog("线程4 loginUser1：{}", LogUtil.toJsonString(loginUser4)));

                new Thread(new TraceRunnable(() -> {
                    LogUtil.resetModule("线程5");
                    log.info(LogUtil.getLog("线程5"));
                    LoginUser loginUser5 = LoginUserUtil.getLoginUser();
                    log.info(LogUtil.getLog("线程5 loginUser5：{}", LogUtil.toJsonString(loginUser5)));
                })).start();

            })).start();
        });

        return SingleResponse.ok(null);
    }

}