package com.aircraftcarrier.framework.concurrent.comletablefuture;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
    public void test0101() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<Void> setLocShopName = runAsync(() -> setFinalPrice(), "setLocShopName").thenAccept((o) -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.info("all done 1 {}", Thread.currentThread().getName());
        });
        CompletableFuture<Void> setLocShopName2 = runAsync(() -> setFinalPrice(), "setLocShopName").thenAccept((o) -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.info("all done 2 {}", Thread.currentThread().getName());
        });
        CompletableFuture<Void> setLocShopName3 = runAsync(() -> setFinalPrice(), "setLocShopName").thenAccept((o) -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.info("all done 3 {}", Thread.currentThread().getName());
        });

        // 不会等待 thenAccept
//         CompletableFuture
//                 .allOf(setLocShopName, setLocShopName2, setLocShopName3)
// //                .get(4000, TimeUnit.MILLISECONDS);
//                 .get();

        CompletableFuture<Void> allOf = CompletableFuture
                .allOf(setLocShopName, setLocShopName2, setLocShopName3);
        allOf.thenRun(() -> {
            System.out.println("all finish");
        });
        allOf.get();
        log.info("main end");
    }

    private void setFinalPrice() {
        log.info("set price .... start... {}", Thread.currentThread().getName());
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("set price .... end {}", Thread.currentThread().getName());
    }

    public CompletableFuture<Void> runAsync(Runnable runnable, String method) {
        return CompletableFuture.runAsync(runnable).exceptionally(e -> {
            log.error("执行异步方法异常, method -> " + method, e);
            return null;
        });
    }


    @Test
    public void test01() throws ExecutionException, InterruptedException {
        List<CompletableFuture<Order>> list = new ArrayList<>();
        List<CompletableFuture<Void>> list2 = new ArrayList<>();
        // example1
        for (int i = 0; i < 3; i++) {
            int finalI = i;
            CompletableFuture<Order> cf = CompletableFuture.supplyAsync(() -> multiThreadPlusExe.getOrder(String.valueOf(finalI)), fjp)
                    .thenApplyAsync(order -> multiThreadPlusExe.enrichOrder(order), cpuBound)
                    .thenApplyAsync(o -> multiThreadPlusExe.performPayment(o), ioBound)
                    .exceptionally(e -> {
                        log.error("failedOrder: " + e.getMessage());
                        // return new Order();
                        // return null;
                        throw new RuntimeException("###########");
                    })
                    .thenApplyAsync(order -> multiThreadPlusExe.dispatchOrder(order));

            // wait for pool until run finish!
            // log.info("wait for pool until run finish!");
            // 等待以上都完成
//             MultiThreadPlusExe.sleep(8000);

            // main thread run
            log.info("main thread run!");

            // 如果以上都完成，那么一下thenApply thenAccept thenRun 会使用main线程执行, 否则继续使用ForkJoinPool.commonPool-worker
            CompletableFuture<Void> cf2 = cf.thenApply(order -> multiThreadPlusExe.sendEmail(order))
                    .thenAccept(order -> log.info("当前订单{}总耗时：{} {}", order.getI(), System.currentTimeMillis() - order.getStart(), Thread.currentThread().getName()))
                    // log
                    .thenRun(() -> log.info("all done {}", Thread.currentThread().getName()))
                    .thenRun(() -> log.info("not really {}", Thread.currentThread().getName()))
                    .thenRun(() -> log.info("keep on going {}", Thread.currentThread().getName()));

            list.add(cf);
            list2.add(cf2);
        }

        // test wait 1
        // wait for pool until run finish!
        log.info("main running wait for pool until run finish!");
//        MultiThreadPlusExe.sleep(10000);
        // test wait 2 等待到thenApply（sendEmail）, 不等待thenAccept,thenRun
        // todo why 不等待 thenAccept,thenRun ???
//        for (CompletableFuture<Order> c : list) {
//            Order order = c.get(); // c.join()
//            log.info("order: {}", JSON.toJSONString(order));
//        }
        CompletableFuture
                .allOf(list2.toArray(new CompletableFuture[0]))
//                .get();
                .join();
        // test wait 3 等待 thenAccept,thenRun完成
        // wait for thenAccept，thenRun until run finish!
        log.info("main running wait for thenAccept，thenRun  until run finish!");
//        MultiThreadPlusExe.sleep(3000);

        log.info("main stop ！");
    }
}
