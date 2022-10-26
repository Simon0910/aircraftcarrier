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
    public void test01() {
        // example1
        for (int i = 0; i < 1; i++) {
            String finalI = String.valueOf(i);
            CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> multiThreadPlusExe.getOrder(finalI), fjp)
                    .thenApplyAsync(order -> multiThreadPlusExe.enrichOrder(order + finalI), cpuBound)
                    .thenApplyAsync(o -> multiThreadPlusExe.performPayment(o + finalI), ioBound)
                    .exceptionally(e -> "failedOrder" + finalI)
                    .thenApplyAsync(order -> multiThreadPlusExe.dispatchOrder(order + finalI));

            // wait for pool until run finish!
            log.info("wait for pool until run finish!");
            MultiThreadPlusExe.sleep(8000);

            // main thread run
            log.info("main thread run!");
            cf.thenAccept(order -> multiThreadPlusExe.sendEmail(order + finalI))
                    .thenRun(() -> log.info("all done"))
                    .thenRun(() -> log.info("not really"))
                    .thenRun(() -> log.info("keep on going"));
        }

        // example2
        CompletableFuture<Integer> future = new CompletableFuture<>();
        // java9 support timeout
//        future.completeOnTimeout(500, 3, TimeUnit.SECONDS);
        future.orTimeout(3, TimeUnit.SECONDS);
        MultiThreadPlusExe.process(future);

        MultiThreadPlusExe.sleep(2000);
//        future.complete(2);
//        future.completeExceptionally(new SysException("something went wrong " + Thread.currentThread().getName()));
        MultiThreadPlusExe.sleep(5000);

        // example3
        MultiThreadPlusExe.create(2).thenCombine(MultiThreadPlusExe.create(3), Integer::sum)
                .thenAccept(r -> {
                    log.info("example3 result: {} \t {}", r, Thread.currentThread().getName());
                });

        // example4
        MultiThreadPlusExe.create(2).thenCompose(MultiThreadPlusExe::create)
                .thenAccept(r -> {
                    log.info("example4 result: {} \t {}", r, Thread.currentThread().getName());
                });

        log.info("main running... {}", Thread.currentThread().getName());
    }
}
