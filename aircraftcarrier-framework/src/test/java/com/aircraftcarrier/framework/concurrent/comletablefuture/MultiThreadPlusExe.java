package com.aircraftcarrier.framework.concurrent.comletablefuture;

import com.aircraftcarrier.framework.exception.BizException;
import com.aircraftcarrier.framework.exception.SysException;
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

    public static int handle(Throwable throwable) {

        log.error("handle ERROR: ", throwable);

        // 默认值
        // return 500;

        // This is beyond any repair
        throw new SysException("This is beyond any repair");
    }

    public static void process(CompletableFuture<Integer> future) {
        future
                .exceptionally(MultiThreadPlusExe::handle)
                .thenApply(data -> {
                    log.info("thenApply: [data[{}] * 2] {}", data, Thread.currentThread().getName());
                    return data * 2;
                })
//                .exceptionally(throwable -> {
//                    log.error(">< ", throwable);
//                    return 200;
//                })
                .thenApply(data -> {
                    log.info("thenApply: [data[{}] + 1] {}", data, Thread.currentThread().getName());
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

    public Order getOrder(String i) {
        Order order = new Order();
        order.setI(i);
        log.info("getOder:\t {} \t {}", i, Thread.currentThread().getName());
        try {
            // do task
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return order;
    }

    public Order enrichOrder(Order order) {
        log.info("enrichOrder:\t {} \t {}", order.getI(), Thread.currentThread().getName());
        try {
            // do task
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (order.getI().contains("1")) {
//            throw new BizException("〒_〒");
        }

        return order;
    }

    public Order performPayment(Order order) {
        log.info("performPayment:\t {} \t {}", order.getI(), Thread.currentThread().getName());
        try {
            // do task
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (order.getI().contains("8")) {
            throw new BizException("〒_〒 〒_〒");
        }

        return order;
    }

    public Order dispatchOrder(Order order) {
        log.info("dispatchOrder:\t {} \t {}", order.getI(), Thread.currentThread().getName());
        try {
            // do task
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return order;
    }

    public Order sendEmail(Order order) {
        log.info("sendEmail:\t {} \t {}", order.getI(), Thread.currentThread().getName());
        try {
            // do task
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return order;
    }
}
