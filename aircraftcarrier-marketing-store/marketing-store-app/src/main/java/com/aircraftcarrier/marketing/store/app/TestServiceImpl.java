package com.aircraftcarrier.marketing.store.app;

import com.aircraftcarrier.framework.cache.LockUtil;
import com.aircraftcarrier.framework.concurrent.CallableVoid;
import com.aircraftcarrier.framework.exception.SysException;
import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.framework.support.trace.TraceThreadPoolExecutor;
import com.aircraftcarrier.framework.tookit.BeanMapUtil;
import com.aircraftcarrier.framework.tookit.RequestLimitUtil;
import com.aircraftcarrier.framework.tookit.ThreadPoolUtil;
import com.aircraftcarrier.marketing.store.app.test.executor.TransactionalExe;
import com.aircraftcarrier.marketing.store.app.test.executor.UpdateInventoryExe;
import com.aircraftcarrier.marketing.store.client.TestService;
import com.aircraftcarrier.marketing.store.client.product.request.InventoryRequest;
import com.aircraftcarrier.marketing.store.common.LoginUserInfo;
import com.aircraftcarrier.marketing.store.domain.drools.KieTemplate;
import com.aircraftcarrier.marketing.store.domain.drools.KieUtils;
import com.aircraftcarrier.marketing.store.domain.event.AccountEvent;
import com.aircraftcarrier.marketing.store.domain.model.test.Address;
import com.aircraftcarrier.marketing.store.domain.model.test.Sale;
import com.aircraftcarrier.marketing.store.domain.redis.JedisUtil;
import com.aircraftcarrier.marketing.store.infrastructure.config.reload.ReloadDroolsRules;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lzp
 */
@Slf4j
@Service
public class TestServiceImpl implements TestService {
    private static final int TASK_NUM = 100;
    private final CyclicBarrier barrier = new CyclicBarrier(TASK_NUM);
    private final TraceThreadPoolExecutor threadPool = new TraceThreadPoolExecutor(10, 20, 3000, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100000));
    @Resource
    UpdateInventoryExe updateInventoryExe;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;
    @Resource
    private TransactionalExe transactionalExe;
    @Resource
    private KieTemplate kieTemplate;
    @Resource
    private ReloadDroolsRules reloadDroolsRules;

    @Override
    public void testTransactional() {
        transactionalExe.execute();
    }

    @Override
    public void publishEvent() {
        LoginUserInfo loginUserInfo = new LoginUserInfo();
        loginUserInfo.setUserName("6409825@qq.com");
        applicationEventPublisher.publishEvent(new AccountEvent<>(loginUserInfo));
    }

    @Override
    public String testLock(Serializable id) {
        JedisUtil.set((String) id, (String) id);
        String value = JedisUtil.get((String) id);
        log.info("JedisUtil: " + value);

        LockUtil.lock(id);
        try {

            int s = 0;
            do {
                s++;
                TimeUnit.SECONDS.sleep(1);
                log.warn("计时：" + s);
            } while (s < 25);

        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } finally {
            LockUtil.unLock();
        }
        log.info("success");
        return "success";
    }

    @Override
    public String testLockKey(Serializable id) {
        CountDownLatch latch = new CountDownLatch(TASK_NUM);

        RequestLimitUtil limitUtil = RequestLimitUtil.getInstance();
        for (int i = 0; i < TASK_NUM; i++) {
            String finalI = String.valueOf(id);
            threadPool.execute(() -> {
                try {
//                    barrier.await();

                    String name = Thread.currentThread().getName();
                    boolean require = limitUtil.require(finalI, 3);
                    if (require) {
                        // do task
                        TimeUnit.SECONDS.sleep(3);
                        log.info("sum ok: " + finalI + "_" + name);
                        limitUtil.release(finalI);
                    } else {
                        log.info("sum noo: " + finalI + "_" + name);
                    }

//                } catch (InterruptedException | BrokenBarrierException e) {
                } catch (InterruptedException e) {
                    log.warn("Interrupted!", e);
                    // Restore interrupted state...
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            long start = System.currentTimeMillis();
            latch.await();
            long end = System.currentTimeMillis();
            log.info("耗时：" + (end - start));
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } finally {
            barrier.reset();
        }
        return "end";
    }

    @Override
    public void applyDiscount(Map<String, Object> params) {
        Sale sale = BeanMapUtil.map2Obj(params, Sale.class);
        kieTemplate.execute(sale);
        log.info("执行规则后返回 sale: {}", JSON.toJSONString(sale));

        Address address = BeanMapUtil.map2Obj(params, Address.class);
        kieTemplate.execute(address);
        log.info("执行规则后返回 address: {}", JSON.toJSONString(address));


        KieUtils.updateToVersion(ReloadDroolsRules.content);

        kieTemplate.execute(sale);
        log.info("执行规则后返回 sale2: {}", JSON.toJSONString(sale));

        kieTemplate.execute(address);
        log.info("执行规则后返回 address2: {}", JSON.toJSONString(address));

    }

    @Override
    public void deductionInventory(Serializable goodsNo) {
        long start = System.currentTimeMillis();
        final AtomicInteger success = new AtomicInteger();
        final AtomicInteger fail = new AtomicInteger();

        // 模拟多人抢购商品
        int num = 5000;
        List<CallableVoid> asyncBatchTasks = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            int finalI = i;
            asyncBatchTasks.add(() -> {
                InventoryRequest inventoryRequest = new InventoryRequest();
                inventoryRequest.setGoodsNo((String) goodsNo);
                inventoryRequest.setUserId(String.valueOf(finalI));
                inventoryRequest.setOrderId(String.valueOf(finalI));
                inventoryRequest.setCount(1);
                SingleResponse<Void> response = updateInventoryExe.deductionInventory(inventoryRequest);
                if (response.success()) {
                    log.info("扣减库存 成功");
                    success.incrementAndGet();
                } else {
                    log.info("扣减库存 失败 〒_〒");
                    fail.incrementAndGet();
                }
            });
        }

        ThreadPoolUtil.invokeAllVoid(threadPool, asyncBatchTasks);
        long end = System.currentTimeMillis();
        log.info("耗时：" + (end - start));

        log.info("success: " + success);
        log.info("fail: " + fail);

    }

    @Override
    public void multiThread() {
        List<Callable<String>> batchTasks = new ArrayList<>(10);

        for (int i = 0; i < 10; i++) {
            int finalI = i;
            batchTasks.add(() -> {
                // do task
                TimeUnit.SECONDS.sleep(3);
                if (finalI == 8) {
                    log.error("task_" + finalI + " " + Thread.currentThread().getName() + " 〒_〒 〒_〒 !");
                    // throw e 会被future.get()捕获;@1
                    throw new SysException("ThreadName: " + Thread.currentThread().getName());
                }
                log.info("task_" + finalI + " " + Thread.currentThread().getName());
                return "I'm OK: " + finalI;
            });
        }

        // 异步执行
        List<Future<String>> futures;
        try {
            futures = threadPool.invokeAll(batchTasks);
            // 等待批量任务执行完成。。。
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("invokeAll - Interrupted error: ", e);
            throw new SysException("invokeAll 中断异常", e);
        }

        // 按list顺序获取
        for (Future<String> future : futures) {
            try {
                String result = future.get();
                log.info("get result: {}", result);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("get - Interrupted error: ", e);
//                throw new SysException("get 中断异常", e);
            } catch (ExecutionException e) {
                // 捕获某个线程任务抛出的异常;@1
                log.error("get - Execution error: ", e);
//                throw new SysException("get 执行异常", e);
            }
        }

        log.info("multiThread end");
    }

    @Override
    public void decrBy(String key) {
        long start = System.currentTimeMillis();
        final AtomicInteger success = new AtomicInteger();
        final AtomicInteger fail = new AtomicInteger();
        final AtomicInteger oversold = new AtomicInteger();

        // 模拟多人抢购商品
        int num = 500;
        List<CallableVoid> asyncBatchTasks = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            asyncBatchTasks.add(() -> {
                String inventory = JedisUtil.get(key);
                if (Integer.parseInt(inventory) > 0) {
                    long stockNum = JedisUtil.decrBy(key, 3);
                    if (stockNum < 0) {
                        // 扣库存超卖
//                        System.out.println(stockNum);
                        oversold.incrementAndGet();
                    } else {
                        // 扣库存成功, 可以后续请求
                        // batchInsert成功后, 后台任务处理后续相关事务
                        success.incrementAndGet();
                    }
                } else {
                    // 没有库存了
                    fail.incrementAndGet();
                }
            });
        }

        ThreadPoolUtil.invokeAllVoid(threadPool, asyncBatchTasks);
        long end = System.currentTimeMillis();
        log.info("耗时：" + (end - start));

        log.info("success: " + success);
        log.info("oversold: " + oversold);
        log.info("fail: " + fail);
    }
}









