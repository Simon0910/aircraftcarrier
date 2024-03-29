package com.aircraftcarrier.marketing.store.app;

import com.aircraftcarrier.framework.cache.LockUtil;
import com.aircraftcarrier.framework.concurrent.CallableVoid;
import com.aircraftcarrier.framework.exception.LockNotAcquiredException;
import com.aircraftcarrier.framework.exception.SysException;
import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.framework.support.trace.TraceThreadPoolExecutor;
import com.aircraftcarrier.framework.tookit.RandomUtil;
import com.aircraftcarrier.framework.tookit.RequestLimitUtil;
import com.aircraftcarrier.framework.tookit.ThreadPoolUtil;
import com.aircraftcarrier.marketing.store.app.test.executor.TransactionalExe;
import com.aircraftcarrier.marketing.store.app.test.executor.TransactionalExe2;
import com.aircraftcarrier.marketing.store.app.test.executor.UpdateInventoryExe;
import com.aircraftcarrier.marketing.store.app.test.executor.UpdateInventoryExe2;
import com.aircraftcarrier.marketing.store.client.TestService;
import com.aircraftcarrier.marketing.store.client.product.request.InventoryRequest;
import com.aircraftcarrier.marketing.store.common.LoginUserInfo;
import com.aircraftcarrier.marketing.store.common.enums.DataTypeEnum;
import com.aircraftcarrier.marketing.store.domain.event.AccountEvent;
import com.aircraftcarrier.marketing.store.domain.redis.JedisUtil;
import com.aircraftcarrier.marketing.store.infrastructure.repository.dataobject.DemoDo;
import com.aircraftcarrier.marketing.store.infrastructure.repository.mapper.DemoMapper;
import com.aircraftcarrier.marketing.store.infrastructure.repository.mybatisplus.DemoMybatisPlus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
    UpdateInventoryExe2 updateInventoryExe2;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;
    @Resource
    private TransactionalExe transactionalExe;
    @Resource
    DemoMapper demoMapper;
    @Resource
    DemoMybatisPlus demoMybatisPlus;
    @Resource
    TransactionalExe2 transactionalExe2;

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

        try {
            LockUtil.lock(id);

            int s = 0;
            do {
                s++;
                TimeUnit.SECONDS.sleep(1);
                log.warn("计时：" + s);
            } while (s < 25);

        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } catch (LockNotAcquiredException e) {
            log.error(e.getMessage());
        } finally {
            LockUtil.unLock(id);
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
//                try {
//                    // 间隔
//                    TimeUnit.MILLISECONDS.sleep(RandomUtil.nextInt(1000,1500));
//                } catch (InterruptedException ignored) {
//                }

                InventoryRequest inventoryRequest = new InventoryRequest();
                inventoryRequest.setGoodsNo((String) goodsNo);
                inventoryRequest.setUserId(String.valueOf(finalI));
                inventoryRequest.setOrderId(String.valueOf(finalI));
                inventoryRequest.setCount(1);
//                SingleResponse<Void> response = updateInventoryExe.deductionInventory(inventoryRequest);
                SingleResponse<Void> response = updateInventoryExe2.deductionInventory(inventoryRequest);
                if (response.success()) {
                    log.info("扣减库存 成功");
                    success.incrementAndGet();
                } else {
                    log.info("扣减库存 失败 〒_〒");
                    fail.incrementAndGet();
                }
            });

//            InventoryRequest inventoryRequest = new InventoryRequest();
//            inventoryRequest.setGoodsNo((String) goodsNo);
//            inventoryRequest.setUserId(String.valueOf(finalI));
//            inventoryRequest.setOrderId(String.valueOf(finalI));
//            inventoryRequest.setCount(1);
////            SingleResponse<Void> response = updateInventoryExe.deductionInventory(inventoryRequest);
//            SingleResponse<Void> response = updateInventoryExe2.deductionInventory(inventoryRequest);
//            if (response.success()) {
//                log.info("扣减库存 成功");
//                success.incrementAndGet();
//            } else {
//                log.info("扣减库存 失败 〒_〒");
//                fail.incrementAndGet();
//            }

        }

        // ForkJoinPool vs ThreadPoolExecutor
//        ThreadPoolUtil.invokeAllVoid(threadPool, asyncBatchTasks);
        ThreadPoolUtil.invokeAllVoid(asyncBatchTasks);
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
    public void decrBy(String goodsNo) {
        long start = System.currentTimeMillis();
        final AtomicInteger success = new AtomicInteger();
        final AtomicInteger fail = new AtomicInteger();
        final AtomicInteger oversold = new AtomicInteger();

        // 模拟多人抢购商品
        int num = 500;
        List<CallableVoid> asyncBatchTasks = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            asyncBatchTasks.add(() -> {
                String inventory = JedisUtil.get(goodsNo);
                if (Integer.parseInt(inventory) > 0) {
                    long stockNum = JedisUtil.decrBy(goodsNo, 3);
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

//    @Transactional(rollbackOn = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void recursionTransactional(String str, int i) {
        DemoDo demoDo = new DemoDo();
        demoDo.setBizNo(str + i);
        demoDo.setDescription("222");
        demoDo.setSellerNo("sellerNo");
        demoDo.setSellerName("sellerName");
        demoDo.setDataType(DataTypeEnum.GENERAL);
        if (i > 0) {
            i--;
            recursionTransactional(str, i);
        }
//        if (i == 2) {
//            throw new BizException("222");
//        }
        demoMapper.insert(demoDo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void recursionTransactional2(String str, int i) {
        transactionalExe2.recursionTransactional2(str, i);

//        if (i == 3) {
//            throw new BizException("222");
//        }

    }

    @Override
    public void reentrantLock(String key) {
        // 相当于 num * 8 = 4000 次请求LockUtil，预计 num * 4 = 2000 次请求redis，相同的key可重入
        int num = 500;
        List<CallableVoid> asyncBatchTasks = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            asyncBatchTasks.add(() -> {
                try {
                    log.info("第一次加锁");
                    // 等待一秒钟还没有抢到redis锁，说明竞争太激烈，或者另一个线程抢到锁后执行逻辑太久不释放
                    LockUtil.lockTimeout(key, 1000, 100);
                    LockUtil.lockTimeout(key + "2", 1000, 100);

//                    LockUtil.lock(key);
//                    LockUtil.lock(key + "2");

//                    LockKeyUtil.lock();
//                    LockKeyUtil.lock(key + "2");

                    reentrantLock2(key);
                    log.info("抢到了redis锁, thread: {}", Thread.currentThread().getName());
                    // 执行业务逻辑
                    TimeUnit.MILLISECONDS.sleep(RandomUtil.nextInt(100, 200));
                }
//                catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                catch (TimeoutException e) {
//                    log.error(e.getMessage());
//                }
                finally {
                    log.info("1解锁");
                    LockUtil.unLock(key + "2");
                    LockUtil.unLock(key);
//                    LockKeyUtil.unlock(key + "2");
//                    LockKeyUtil.unlock();
                }

            });
        }

//        ThreadPoolUtil.invokeAllVoid(threadPool, asyncBatchTasks, true);
        ThreadPoolUtil.invokeAllVoid(ThreadPoolUtil.newCachedThreadPool("测试redis锁"), asyncBatchTasks, true);
    }

    private void reentrantLock2(String key) {
        try {
            log.info("第二次加锁");
            LockUtil.lock(key);
            LockUtil.lock(key + "2");
//            LockKeyUtil.lock();
//            LockKeyUtil.lock(key + "2");
            // 执行业务逻辑
            TimeUnit.MILLISECONDS.sleep(RandomUtil.nextInt(100, 200));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (LockNotAcquiredException e) {
            log.error(e.getMessage());
        } finally {
            log.info("2解锁");
            LockUtil.unLock(key + "2");
            LockUtil.unLock(key);
//            LockKeyUtil.unlock(key + "2");
//            LockKeyUtil.unlock();
        }
    }
}









