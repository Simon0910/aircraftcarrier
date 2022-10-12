package com.lzp.comletablefuture;

import com.aircraftcarrier.framework.exception.BizException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author lzp
 */
@Slf4j
public class MultiThreadPlusExe {

    public static boolean sleep(int ms) {
        try {
            Thread.sleep(ms);
            return true;
        } catch (InterruptedException e) {
            return false;
        }

    }

    public String getOrder(String order) {
        log.info("getOder:\t {} \t {}", order, Thread.currentThread().getName());
        try {
            // do task
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "order";
    }

    public String enrichOrder(String order) {
        log.info("enrichOrder:\t {} \t {}", order, Thread.currentThread().getName());
        try {
            // do task
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (order.contains("8")) {
            throw new BizException("〒_〒");
        }

        return "enrichOrder";
    }

    public String performPayment(String order) {
        log.info("performPayment:\t {} \t {}", order, Thread.currentThread().getName());
        try {
            // do task
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (order.contains("8")) {
            throw new BizException("〒_〒 〒_〒");
        }

        return "performPayment";
    }

    public String dispatchOrder(String order) {
        log.info("dispatchOrder:\t {} \t {}", order, Thread.currentThread().getName());
        try {
            // do task
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "dispatchOrder";
    }

    public String sendEmail(String order) {
        log.info("sendEmail:\t {} \t {}", order, Thread.currentThread().getName());
        try {
            // do task
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "sendEmail";
    }

    public static int handle(Throwable throwable) {
        log.error("ERROR: ", throwable);
//        return 500;
        // This is beyond any repair
        throw new RuntimeException("This is beyond any repair");
    }

    public static void process(CompletableFuture<Integer> future) {
        future
                .exceptionally(MultiThreadPlusExe::handle)
                .thenApply(data -> {
                    log.info("thenApply: [data * 2] {}", Thread.currentThread().getName());
                    return data * 2;
                })
//                .exceptionally(throwable -> {
//                    log.error(">< ", throwable);
//                    return 200;
//                })
                .thenApply(data -> {
                    log.info("thenApply: [data + 1] {}", Thread.currentThread().getName());
                    return data + 1;
                })
                .thenAccept(data -> {
                    log.info("process result data: {} {}", data, Thread.currentThread().getName());
                });
    }

    public static CompletableFuture<Integer> create(int n) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("create {}, thread: {}", n, Thread.currentThread().getName());
            return n;
        });
    }
}
