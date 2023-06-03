package com.aircraftcarrier.marketing.store.app;

import com.aircraftcarrier.framework.cache.LockUtil;
import com.aircraftcarrier.framework.cache.LockUtil2;
import com.aircraftcarrier.framework.concurrent.CallableVoid;
import com.aircraftcarrier.framework.concurrent.ExecutorUtil;
import com.aircraftcarrier.framework.concurrent.ThreadPoolUtil;
import com.aircraftcarrier.framework.concurrent.TraceThreadPoolExecutor;
import com.aircraftcarrier.framework.exception.LockNotAcquiredException;
import com.aircraftcarrier.framework.exception.SysException;
import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.framework.tookit.RandomUtil;
import com.aircraftcarrier.framework.tookit.RequestLimitUtil;
import com.aircraftcarrier.framework.tookit.SleepUtil;
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
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
import java.util.concurrent.atomic.LongAdder;

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
    ApplicationEventPublisher applicationEventPublisher;
    @Resource
    TransactionalExe transactionalExe;
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
        String test = JedisUtil.get("abc");
        if (StringUtils.hasText(value)) {
            JedisUtil.expire((String) id, 0);
        }
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
        List<CallableVoid> asyncBatchActions = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            asyncBatchActions.add(() -> {
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

        ThreadPoolUtil.invokeAllVoid(threadPool, asyncBatchActions);
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
        long start = System.currentTimeMillis();
        LongAdder success = new LongAdder();
        // 相当于 num * 8 = 4000 次请求LockUtil，预计 num * 4 = 2000 次请求redis，相同的key可重入
        int num = 500;
        List<CallableVoid> asyncBatchActions = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            String lockKey = String.valueOf(key);
            // String lockKey = String.valueOf(i);
            String lockKey2 = lockKey + "Two";
            asyncBatchActions.add(() -> {
                try {
                    log.info("第一次加锁");
                    // 等待一秒钟还没有抢到redis锁，说明竞争太激烈，或者另一个线程抢到锁后执行逻辑太久不释放
                    // LockUtil.lockTimeout(lockKey, 1000, 10);
                    // LockUtil.lockTimeout(lockKey2, 1000, 10);

                    // LockUtil.lock(lockKey);
                    // LockUtil.lock(lockKey2);

                    // LockUtils.lockMillis(lockKey, 30000, 1000);
                    // LockUtils.lockMillis(lockKey2, 30000, 1000);

                    // LockKeyUtil.lock();
                    // LockKeyUtil.lock(lockKey2);

                    // reentrantLock2(lockKey, lockKey2);

                    boolean b = LockUtil2.tryLock(lockKey, 60000, 50, TimeUnit.MILLISECONDS);
                    // boolean b = LockUtil2.tryLock(lockKey, 60000, 3000, TimeUnit.MILLISECONDS);
                    if (!b) {
                        return;
                    }

                    log.info("抢到了redis锁, thread: {}", Thread.currentThread().getName());
                    // 执行业务逻辑
                    // TimeUnit.MILLISECONDS.sleep(RandomUtil.nextInt(10000, 20000));
                    success.increment();
                }
//                catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                catch (TimeoutException e) {
//                    log.error(e.getMessage());
//                }
                catch (LockNotAcquiredException e) {
                    log.error(e.getMessage());
                } finally {
                    log.info("1解锁");

                    LockUtil2.unLock(lockKey);

                    // LockUtil.unLock(lockKey2);
                    // LockUtil.unLock(lockKey);

                    // LockUtils.unLock(lockKey2);
                    // LockUtils.unLock(lockKey);

                    // LockKeyUtil.unlock(lockKey2);
                    // LockKeyUtil.unlock();
                }

            });
        }

//        ThreadPoolUtil.invokeAllVoid(threadPool, asyncBatchTasks, true);
        ThreadPoolUtil.invokeAllVoid(ExecutorUtil.newCachedThreadPoolBlock(asyncBatchActions.size(), "测试redis锁"), asyncBatchActions);
        log.info("success: {}, 耗时：{}", success, System.currentTimeMillis() - start);
    }

    /**
     * https://www.cnblogs.com/hollischuang/p/15522907.html
     *
     * @param param param
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void isolation(String param) {
        // 第2个线程可以看到第1个线程保存的数据吗？@Transactional会有影响吗？
        LambdaQueryWrapper<DemoDo> queryWrapper = new LambdaQueryWrapper<DemoDo>()
                .in(DemoDo::getSellerNo, param);
        // 线程2可以看到线程1的新增的第一条记录?
        List<DemoDo> list = demoMapper.selectList(queryWrapper);
        log.info("第一次查询 list ==> {}", JSON.toJSONString(list));

        DemoDo demoDo = new DemoDo();
        demoDo.setBizNo("bizNo");
        demoDo.setDescription("desc");
        demoDo.setSellerNo(param);
        demoDo.setSellerName("isolation");
        demoDo.setDataType(DataTypeEnum.GENERAL);
        demoMapper.insert(demoDo);
        log.info("第1次新增成功");

        // 线程2可以看到线程1的新增的第一条记录？
        list = demoMapper.selectList(queryWrapper);
        log.info("第二次查询 list ==> {}", JSON.toJSONString(list));

        SleepUtil.sleepSeconds(5);

        // 重置主键
        demoDo.setId(null);
        demoMapper.insert(demoDo);
        log.info("第2次新增成功");

        // 线程2可以看到线程1的新增的第二条记录？
        list = demoMapper.selectList(queryWrapper);
        log.info("第三次查询 list ==> {}", JSON.toJSONString(list));

        SleepUtil.sleepSeconds(8);
        System.out.println("end");
    }

    private void reentrantLock2(String key, String key2) {
        try {
            log.info("第二次加锁");

            LockUtil.lock(key);
            LockUtil.lock(key2);

            // LockUtils.lockMillis(key, 30000, 3000);
            // LockUtils.lockMillis(key2, 30000, 3000);

            // LockKeyUtil.lock();
            // LockKeyUtil.lock(key2);
            // 执行业务逻辑
            TimeUnit.MILLISECONDS.sleep(RandomUtil.nextInt(10, 20));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (LockNotAcquiredException e) {
            log.error(e.getMessage());
        } finally {
            log.info("2解锁");

            LockUtil.unLock(key2);
            LockUtil.unLock(key);

            // LockUtils.unLock(key);
            // LockUtils.unLock(key2);

            // LockKeyUtil.unlock(key2);
            // LockKeyUtil.unlock();
        }
    }
}









