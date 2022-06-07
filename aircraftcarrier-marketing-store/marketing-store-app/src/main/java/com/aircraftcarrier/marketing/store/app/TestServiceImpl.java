package com.aircraftcarrier.marketing.store.app;

import com.aircraftcarrier.framework.cache.LockUtil;
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
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lzp
 */
@Slf4j
@Service
public class TestServiceImpl implements TestService {
    private final int threadNum = 100;
    private final CyclicBarrier barrier = new CyclicBarrier(threadNum);
    private final TraceThreadPoolExecutor pool = new TraceThreadPoolExecutor(threadNum, threadNum, 3000, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;
    @Resource
    private TransactionalExe transactionalExe;
    @Resource
    private KieTemplate kieTemplate;
    @Resource
    private ReloadDroolsRules reloadDroolsRules;
    @Resource
    UpdateInventoryExe updateInventoryExe;

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
        CountDownLatch latch = new CountDownLatch(threadNum);

        RequestLimitUtil limitUtil = RequestLimitUtil.getInstance();
        for (int i = 0; i < threadNum; i++) {
            String finalI = String.valueOf(id);
//            String finalI = String.valueOf(i);
            pool.execute(() -> {
                try {
                    barrier.await();

                    String name = Thread.currentThread().getName();
                    boolean require = limitUtil.require(finalI);
                    if (require) {
                        System.out.println("sum ok: " + finalI + "_" + name);
                        limitUtil.release(finalI);
                    } else {
                        System.out.println("sum noo: " + finalI + "_" + name);
                    }

                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            long start = System.currentTimeMillis();
            latch.await();
            long end = System.currentTimeMillis();
            System.out.println("耗时：" + (end - start));
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
        final CountDownLatch latch = new CountDownLatch(threadNum);
        final AtomicInteger success = new AtomicInteger();
        final AtomicInteger fail = new AtomicInteger();

        // 模拟多人抢购商品
        for (int i = 0; i < threadNum; i++) {
            pool.execute(() -> {
                try {

                    SingleResponse<Void> response = updateInventoryExe.deductionInventory(goodsNo);
                    if (response.success()) {
                        System.out.println("扣减库存 成功");
                        success.incrementAndGet();
                    } else {
                        System.out.println("扣减库存 失败 〒_〒");
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
            System.out.println("耗时：" + (end - start));
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        System.out.println("success: " + success);
        System.out.println("fail: " + fail);

    }
}