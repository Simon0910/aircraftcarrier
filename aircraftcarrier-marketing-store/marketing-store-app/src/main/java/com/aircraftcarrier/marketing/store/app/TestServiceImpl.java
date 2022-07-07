package com.aircraftcarrier.marketing.store.app;

import com.aircraftcarrier.framework.cache.LockUtil;
import com.aircraftcarrier.framework.exception.SysException;
import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.framework.support.trace.TraceThreadPoolExecutor;
import com.aircraftcarrier.framework.tookit.ObjUtil;
import com.aircraftcarrier.framework.tookit.RequestLimitUtil;
import com.aircraftcarrier.marketing.store.app.test.executor.TransactionalExe;
import com.aircraftcarrier.marketing.store.app.test.executor.UpdateInventoryExe;
import com.aircraftcarrier.marketing.store.client.TestService;
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
import java.util.concurrent.BrokenBarrierException;
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
    private static final int THREAD_NUM = 100;
    private final CyclicBarrier barrier = new CyclicBarrier(THREAD_NUM);
    private final TraceThreadPoolExecutor threadPool = new TraceThreadPoolExecutor(THREAD_NUM, THREAD_NUM, 3000, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
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
        CountDownLatch latch = new CountDownLatch(THREAD_NUM);

        RequestLimitUtil limitUtil = RequestLimitUtil.getInstance();
        for (int i = 0; i < THREAD_NUM; i++) {
            String finalI = String.valueOf(id);
            threadPool.execute(() -> {
                try {
                    barrier.await();

                    String name = Thread.currentThread().getName();
                    boolean require = limitUtil.require(finalI);
                    if (require) {
                        log.info("sum ok: " + finalI + "_" + name);
                        limitUtil.release(finalI);
                    } else {
                        log.info("sum noo: " + finalI + "_" + name);
                    }

                } catch (InterruptedException | BrokenBarrierException e) {
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
        Sale sale = ObjUtil.map2Obj(params, Sale.class);
        kieTemplate.execute(sale);
        log.info("执行规则后返回 sale: {}", JSON.toJSONString(sale));

        Address address = ObjUtil.map2Obj(params, Address.class);
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
        final CountDownLatch latch = new CountDownLatch(THREAD_NUM);
        final AtomicInteger success = new AtomicInteger();
        final AtomicInteger fail = new AtomicInteger();

        // 模拟多人抢购商品
        for (int i = 0; i < THREAD_NUM; i++) {
            threadPool.execute(() -> {
                try {

                    SingleResponse<Void> response = updateInventoryExe.deductionInventory(goodsNo);
                    if (response.success()) {
                        log.info("扣减库存 成功");
                        success.incrementAndGet();
                    } else {
                        log.info("扣减库存 失败 〒_〒");
                        fail.incrementAndGet();
                    }

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
        }

        log.info("success: " + success);
        log.info("fail: " + fail);

    }

    @Override
    public void multiThread() {
        List<Callable<Boolean>> batchTasks = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            int finalI = i;
            batchTasks.add(() -> {
                // do task
                TimeUnit.SECONDS.sleep(3);
                log.error("task_" + finalI);
                return true;
            });
        }

        try {
            List<Future<Boolean>> futures = threadPool.invokeAll(batchTasks);
            for (Future<Boolean> future : futures) {
                Boolean aBoolean = future.get();
                log.info("result: {}", aBoolean);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } catch (ExecutionException ignore) {
            throw new SysException("ignore");
        }

        log.error("multiThread end");
    }
}









