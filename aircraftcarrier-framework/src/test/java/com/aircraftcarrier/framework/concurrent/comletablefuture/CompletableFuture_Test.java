package com.aircraftcarrier.framework.concurrent.comletablefuture;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * https://www.youtube.com/watch?v=9ueIL0SwEWI
 */
@Slf4j
public class CompletableFuture_Test {
    static MultiThreadPlusExe multiThreadPlusExe = new MultiThreadPlusExe();
    static ForkJoinPool fjp = new ForkJoinPool(10);
    static ExecutorService cpuBound = Executors.newFixedThreadPool(4);
    static ExecutorService ioBound = Executors.newCachedThreadPool();

    @Test
    public void test04() {
        // example4
        MultiThreadPlusExe.create(2).thenCompose(MultiThreadPlusExe::create)
                .thenAccept(r -> {
                    log.info("example4 result: {} \t {}", r, Thread.currentThread().getName());
                });

        log.info("main running... {}", Thread.currentThread().getName());
    }

    @Test
    public void test03() {
        // example3
        MultiThreadPlusExe.create(2).thenCombine(MultiThreadPlusExe.create(3), Integer::sum)
                .thenAccept(r -> {
                    log.info("example3 result: {} \t {}", r, Thread.currentThread().getName());
                });

        log.info("main running... {}", Thread.currentThread().getName());
    }

    @Test
    public void test02() {
        // example2
        CompletableFuture<Integer> future = new CompletableFuture<>();

        // java9 support timeout 默认值
        future.orTimeout(3, TimeUnit.SECONDS);
        // future.completeOnTimeout(500, 3, TimeUnit.SECONDS);

        MultiThreadPlusExe.process(future);

        // complete future = 2.
        // System.out.println("complete future = 2.");
        // future.complete(2);

        // complete throw Exception
        // System.out.println("complete Exception.");
        // future.completeExceptionally(new SysException("something went wrong " + Thread.currentThread().getName()));

        MultiThreadPlusExe.sleep(5000);
        log.info("main end !");
    }

    @Test
    public void test01() {
        // example1
        for (int i = 0; i < 3; i++) {
            int finalI = i;
            CompletableFuture<Order> cf = CompletableFuture.supplyAsync(() -> multiThreadPlusExe.getOrder(String.valueOf(finalI)), fjp)
                    .thenApplyAsync(order -> multiThreadPlusExe.enrichOrder(order), cpuBound)
                    .thenApplyAsync(o -> multiThreadPlusExe.performPayment(o), ioBound)
                    .exceptionally(e -> {
                        System.out.println("failedOrder: " + e.getMessage());
                        // return new Order();
                        // return null;
                        throw new RuntimeException("###########");
                    })
                    .thenApplyAsync(order -> multiThreadPlusExe.dispatchOrder(order));

            // wait for pool until run finish!
            // log.info("wait for pool until run finish!");
            // MultiThreadPlusExe.sleep(8000);

            // main thread run
            log.info("main thread run!");

            cf.thenApply(order -> multiThreadPlusExe.sendEmail(order))
                    .thenAccept(order -> log.info("当前订单{}总耗时：{}", order.getI(), System.currentTimeMillis() - order.getStart()))
                    // log
                    .thenRun(() -> log.info("all done"))
                    .thenRun(() -> log.info("not really"))
                    .thenRun(() -> log.info("keep on going"));
        }

        // wait for pool until run finish!
        log.info("main running wait for pool until run finish!");
        MultiThreadPlusExe.sleep(10000);

        log.info("main stop ！");
    }
}
