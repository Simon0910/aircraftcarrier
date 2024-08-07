package com.aircraftcarrier.framework.concurrent.multithreadcallapi;

import com.aircraftcarrier.framework.concurrent.CallParallelTask;
import com.aircraftcarrier.framework.concurrent.CallRecursiveTask;
import com.aircraftcarrier.framework.concurrent.ThreadPoolUtil;
import com.aircraftcarrier.framework.tookit.SleepUtil;
import com.aircraftcarrier.framework.tookit.TimeLogUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author liuzhipeng
 */
@Slf4j
public class CallApiRecursiveTaskTest {

    private static final int num = 1000;

    private static final List<Param> params = new ArrayList<>(num);
    private static final AtomicLong atomicLong = new AtomicLong();
    private static final LongAdder longAdder = new LongAdder();
    private static final LongAccumulator longAccumulator = new LongAccumulator(Long::sum, 0);
    private static CallApiService callApiService;

    /**
     * todo  1. LongAdder why not ? 使用场景？
     * todo  2. corePoolSize == 0时 一定要用SynchronousQueue？ 不然就可能串行无限执行了！！
     * 参考：
     * {@link java.util.concurrent.Executors#newCachedThreadPool() }
     * {@link cn.hutool.core.thread.ExecutorBuilder#build(cn.hutool.core.thread.ExecutorBuilder) }
     * todo  3. https://www.bilibili.com/video/BV1M34y1q7M2/?spm_id_from=333.999.0.0&vd_source=5ae6c4b2dbcbc1516cef3f31fbe2abb2
     * todo  4. https://cloud.tencent.com/developer/article/1366581
     * todo  5. https://mp.weixin.qq.com/s/BX-EvTOMWc8d4H0mK6wpSA
     * todo  6. https://tech.meituan.com/2022/05/12/principles-and-practices-of-completablefuture.html
     */
    @Before
    public void before() {
        for (int i = 0; i < num; i++) {
            Param param = new Param();
            param.setI(i);
            params.add(param);
        }
        callApiService = (param) -> {
            log.info("n");
//            longAdder.increment();
            longAccumulator.accumulate(1);
//            SleepUtil.sleepMilliseconds(RandomUtil.nextInt(100, 200));
            SleepUtil.sleepMilliseconds(200);

            Result result = new Result();
            result.setId(1L);
            result.setName("name" + atomicLong.incrementAndGet());
//            result.setName("name" + longAdder.longValue()); // why not ?
//            result.setName("name" + longAccumulator.get());
            result.setDate(new Date());
//            log.info(">>>>>>>>>>> {}", JsonUtil.toJson(result));
            if (param.getI() == 5) {
//                int n = 1 / 0;
            }
            return result;
        };
    }

    @After
    public void after() {
        System.out.println("longAccumulator1 =======>" + longAccumulator.get());
        longAccumulator.reset();
        System.out.println("longAccumulator2 =======>" + longAccumulator.get());
    }

    @Test
    public void testCall_For() {
        long l = TimeLogUtil.startTime();
        List<Result> results = new ArrayList<>(num);
        for (Param param : params) {
            Result result = callApiService.getResult(param);
            results.add(result);
        }

        System.out.println("for ===> " + results.size());
        TimeLogUtil.logElapsedTime(l);

        Assert.isTrue(num == results.stream().map(Result::getName).distinct().count(), "error");
    }

    @Test
    public void testCall_RecursiveTask() {
        // 底层使用默认 ForkJoinPool.commonPool()
        long l = TimeLogUtil.startTime();
        CallRecursiveTask<Param, Result> task = new CallRecursiveTask<>((param) -> callApiService.getResult(param), params);

//        task.fork();
//        List<Result> results = task.join();

        List<Result> results = ThreadPoolUtil.invoke(task, 500);

        System.out.println("RecursiveTask ===> " + results.size());
        TimeLogUtil.logElapsedTime(l);

        Assert.isTrue(num == results.stream().map(Result::getName).distinct().count(), "error");
    }

    @Test
    public void testCall_ForkJoinPool() {
        // https://www.bilibili.com/video/BV1M34y1q7M2/?spm_id_from=333.999.0.0&vd_source=5ae6c4b2dbcbc1516cef3f31fbe2abb2
        // ForkJoinPool 使用场景主要时为了解决流式处理，有依赖关系的操作避免队列引起的死循环 如stream底层，CompletableFuture底层
        long l = TimeLogUtil.startTime();

        // example01
        // List<Callable<Result>> taskList = new ArrayList<>(num);
        // for (Param param : params) {
        //     taskList.add(() -> callApiService.getResult(param));
        // }
        // ExecutorService executorService = ExecutorUtil.newWorkStealingPool(1000, "call bbb");
        // List<Result> results = ThreadPoolUtil.invokeAll(executorService, taskList);

        // example02
        CallParallelTask<Param, Result> parallelTask = new CallParallelTask<>((param) -> callApiService.getResult(param), params);
        List<Result> results = ThreadPoolUtil.invokeTask(parallelTask, 500);

        System.out.println("ForkJoinPool ===> " + results.size());
        TimeLogUtil.logElapsedTime(l);

        Assert.isTrue(num == results.stream().map(Result::getName).distinct().count(), "error");
    }

    @Test
    public void testCall_ThreadPoolExecutor() {
        long l = TimeLogUtil.startTime();

        // example01
        // List<Callable<Result>> taskList = new ArrayList<>(num);
        // for (Param param : params) {
        //     taskList.add(() -> callApiService.getResult(param));
        // }
        // ExecutorService executorService = ExecutorUtil.newCachedThreadPool("call ccc");
        // List<Result> results = ThreadPoolUtil.invokeAll(executorService, taskList);

        // example03
        CallParallelTask<Param, Result> parallelTask = new CallParallelTask<>((param) -> callApiService.getResult(param), params);
        List<Result> results = ThreadPoolUtil.invokeTask(parallelTask, 1000, "call ccc");

        System.out.println("ThreadPoolExecutor ===> " + results.size());
        TimeLogUtil.logElapsedTime(l);

        Assert.isTrue(num == results.stream().map(Result::getName).distinct().count(), "error");
    }
}
