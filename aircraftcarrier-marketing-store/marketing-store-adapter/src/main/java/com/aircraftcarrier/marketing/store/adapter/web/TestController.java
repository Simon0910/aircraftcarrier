package com.aircraftcarrier.marketing.store.adapter.web;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.framework.scheduling.DynamicTaskService;
import com.aircraftcarrier.framework.security.core.LoginUser;
import com.aircraftcarrier.framework.security.core.LoginUserUtil;
import com.aircraftcarrier.framework.support.trace.MdcRunnableDecorator;
import com.aircraftcarrier.framework.support.trace.TraceThreadPoolExecutor;
import com.aircraftcarrier.framework.tookit.JsonUtil;
import com.aircraftcarrier.marketing.store.adapter.scheduler.PrintTimeTask;
import com.aircraftcarrier.marketing.store.client.TestService;
import com.aircraftcarrier.marketing.store.client.demo.request.DemoRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    private ThreadLocal<String> threadLocal = new ThreadLocal<>();
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
        log.info("LoginUser：{}", JsonUtil.toJson(loginUser));

        new Thread(new MdcRunnableDecorator(() -> {
            log.info("1-获取主线程的MDC上下文,例如traceId");
            LoginUser loginUser1 = LoginUserUtil.getLoginUser();
            log.info("1-LoginUser：{}", JsonUtil.toJson(loginUser1));
        })).start();

        testService.publishEvent();

        threadPoolExecutor.execute(() -> {
            log.info("2-获取主线程的MDC上下文,例如traceId");
            LoginUser loginUser2 = LoginUserUtil.getLoginUser();
            log.info("2-LoginUser：{}", JsonUtil.toJson(loginUser2));
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
        for (int i = 0; i < 1; i++) {
            testService.testLockKey(id);
        }
        return SingleResponse.ok();
    }

    @ApiOperationSupport(order = 36)
    @ApiOperation(value = "并发扣库存防止超卖")
    @GetMapping("/deductionInventory")
    public SingleResponse<String> deductionInventory(@RequestParam Serializable goodsNo) {
        // jmeter 模拟测试
        for (int i = 0; i < 2; i++) {
            testService.deductionInventory(goodsNo);
        }
        return SingleResponse.ok();
    }

    @ApiOperationSupport(order = 37)
    @ApiOperation(value = "多线程测试invokeAll")
    @GetMapping("/multiThread")
    public SingleResponse<String> multiThread() {
        for (int i = 0; i < 1; i++) {
            testService.multiThread();
        }
        return SingleResponse.ok();
    }

    @ApiOperationSupport(order = 40)
    @ApiOperation(value = "接受jsonString")
    @PostMapping("/receiveJson")
    public SingleResponse<String> receiveJsonStr(HttpServletRequest request) throws Exception {
        System.out.println("in");
        ServletInputStream in = request.getInputStream();

        try {
            byte[] bytes = in.readAllBytes();
            String requestParams = new String(bytes, StandardCharsets.UTF_8);
            System.out.println(requestParams);

            JSONObject jsonObject = JSON.parseObject(requestParams);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return SingleResponse.ok("okk");
    }

    @ApiOperationSupport(order = 45)
    @ApiOperation(value = "getAccessToken")
    @PostMapping("/getAccessToken")
    public SingleResponse<String> getAccessToken(@RequestHeader("Authorization") String auth,
                                                 @RequestBody String body) {
        System.out.println("auth: " + auth);
        System.out.println("body: " + JSON.toJSONString(body));
        return SingleResponse.ok("okk");
    }

    @ApiOperationSupport(order = 45)
    @ApiOperation(value = "通知消息")
    @PostMapping("/notifyMessage")
    public SingleResponse<String> notifyMessage(@RequestHeader("Authorization") String auth,
                                                @RequestBody String messageBody) {
        System.out.println("auth: " + auth);
        System.out.println("messageBody: " + JSON.toJSONString(messageBody));
        return SingleResponse.ok("okk");
    }

    /**
     * <a href="https://www.baeldung.com/java-memory-leaks">...</a>
     */
    @ApiOperationSupport(order = 46)
    @ApiOperation(value = "threadLocal")
    @GetMapping("/threadLocal")
    public SingleResponse<String> threadLocal(String value) {
        System.out.println("threadLocal");

        threadPoolExecutor.execute(() -> {
            threadLocal.set(value);
            String s = threadLocal.get();
            System.out.println(s);
            Thread thread = Thread.currentThread();
        });

        return SingleResponse.ok("okk");
    }

    @ApiOperationSupport(order = 47)
    @ApiOperation(value = "gc")
    @GetMapping("/gc")
    public SingleResponse<String> gc() {
        System.out.println("gc");
        threadLocal = null;
        System.gc();
        threadLocal = new ThreadLocal<>();
        return SingleResponse.ok("gc");
    }

    @ApiOperationSupport(order = 48)
    @ApiOperation(value = "分布式自减decr")
    @GetMapping("/decr")
    public SingleResponse<String> decr(String key) {
        System.out.println("decr");
        for (int i = 0; i < 2; i++) {
            testService.decrBy(key);
        }
        return SingleResponse.ok("decr");
    }

    @ApiOperationSupport(order = 49)
    @ApiOperation(value = "递归事务测试")
    @GetMapping("/recursionTransactional")
    public SingleResponse<String> recursionTransactional(String str) {
        System.out.println("recursion Transactional");
        for (int i = 0; i < 2; i++) {
            testService.recursionTransactional(str, 3);
        }
        return SingleResponse.ok("recursion Transactional");
    }

    @ApiOperationSupport(order = 50)
    @ApiOperation(value = "递归事务测试2")
    @GetMapping("/recursionTransactional2")
    public SingleResponse<String> recursionTransactional2(String str) {
        System.out.println("recursion Transactional");
        for (int i = 0; i < 2; i++) {
            testService.recursionTransactional2(str, 3);
        }
        return SingleResponse.ok("recursion Transactional");
    }

    @ApiOperationSupport(order = 51)
    @ApiOperation(value = "可重入锁")
    @GetMapping("/reentrantLock")
    public SingleResponse<String> reentrantLock(String key) {
        System.out.println("reentrantLock");
        testService.reentrantLock(key);
        return SingleResponse.ok("reentrantLock");
    }

    private final Map<String, DynamicTaskService.AbstractAsyncTask> taskTask = new ConcurrentHashMap<>();

    @ApiOperationSupport(order = 52)
    @ApiOperation(value = "引用测试")
    @GetMapping("/reference")
    public SingleResponse<String> reference() {
        for (int i = 0; i < 10000; i++) {
            PrintTimeTask task = new PrintTimeTask("cron");

            taskTask.put(task.getTaskName(), task);
//            task.holdTaskMap(taskTask);

            threadPoolExecutor.execute(() -> {
//                task.removeTask(task);
            });

            System.out.println(taskTask.size());
            System.out.println();
        }
        return SingleResponse.ok("reference");
    }

}
