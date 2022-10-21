package com.lzp.multithreadcallapi;

import com.aircraftcarrier.framework.tookit.JsonUtil;
import com.aircraftcarrier.framework.tookit.RandomUtil;
import com.aircraftcarrier.framework.tookit.SleepUtil;
import com.aircraftcarrier.framework.tookit.ThreadPoolUtil;
import com.aircraftcarrier.framework.tookit.TimeLogUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author liuzhipeng
 */
@Slf4j
public class CallApiRecursiveTaskTest {

    private static final int num = 10000;

    private static final List<Param> params = new ArrayList<>(num);
    private static final LongAdder longAdder = new LongAdder();
    private static final AtomicLong atomicLong = new AtomicLong();
    private static CallApiService callApiService;

    @Before
    public void before() {
        for (int i = 0; i < num; i++) {
            Param param = new Param();
            params.add(param);
        }
        callApiService = (param) -> {
            longAdder.increment();
            SleepUtil.sleepMilliseconds(RandomUtil.nextInt(100, 200));
            Result result = new Result();
            result.setId(1L);
//            result.setName("name" + longAdder.longValue()); // why not ?
            result.setName("name" + atomicLong.incrementAndGet());
            result.setDate(new Date());
            log.info(">>>>>>>>>>> {}", JsonUtil.toJson(result));
            return result;
        };
    }

    @Test
    public void testCall_For() {
        long l = TimeLogUtil.beginTime();
        List<Result> results = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            Result result = callApiService.getResult(new Param());
            results.add(result);
        }
        System.out.println("for ===> " + results.size());
        System.out.println(TimeLogUtil.endTimeStr(l));

        Assert.isTrue(num == results.stream().map(Result::getName).distinct().count(), "error");
    }

    @Test
    public void testCall_RecursiveTask() {
        // 底层使用默认 ForkJoinPool.commonPool()
        long l = TimeLogUtil.beginTime();
        CallApiRecursiveTask<Param, Result> task = new CallApiRecursiveTask<>((param) -> callApiService.getResult(param), params, Runtime.getRuntime().availableProcessors());
        task.fork();
        List<Result> results = task.join();
        System.out.println("RecursiveTask ===> " + results.size());
        System.out.println(TimeLogUtil.endTimeStr(l));

        Assert.isTrue(num == results.stream().map(Result::getName).distinct().count(), "error");
    }

    @Test
    public void testCall_ForkJoinPool() {
        // https://www.bilibili.com/video/BV1M34y1q7M2/?spm_id_from=333.999.0.0&vd_source=5ae6c4b2dbcbc1516cef3f31fbe2abb2
        // ForkJoinPool 使用场景主要时为了解决流式处理，有依赖关系的操作避免队列引起的死循环 如stream底层，CompletableFuture底层
        long l = TimeLogUtil.beginTime();
        List<Callable<Result>> task = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            task.add(() -> callApiService.getResult(new Param()));
        }

        ExecutorService executorService = ThreadPoolUtil.newWorkStealingPool(1000, "call-api");
        List<Result> results = ThreadPoolUtil.invokeAll(executorService, task);

        System.out.println("ForkJoinPool ===> " + results.size());
        System.out.println(TimeLogUtil.endTimeStr(l));

        Assert.isTrue(num == results.stream().map(Result::getName).distinct().count(), "error");
    }

    @Test
    public void testCall_ThreadPoolExecutor() {
        long l = TimeLogUtil.beginTime();
        List<Callable<Result>> task = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            task.add(() -> callApiService.getResult(new Param()));
        }

        ExecutorService executorService = ThreadPoolUtil.newCachedThreadPool(1000, "call-api");
        List<Result> results = ThreadPoolUtil.invokeAll(executorService, task);

        System.out.println("ThreadPoolExecutor ===> " + results.size());
        System.out.println(TimeLogUtil.endTimeStr(l));

        Assert.isTrue(num == results.stream().map(Result::getName).distinct().count(), "error");
    }
}
