package com.aircraftcarrier.framework.tookit;

import com.aircraftcarrier.framework.concurrent.CallableVoid;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 类注释内容
 *
 * @author zhipengliu
 * @date 2022/8/19
 * @since 1.0
 */
@Slf4j
public class TheadPoolUtilTest {
    /**
     * 批量订单付款消息 线程池
     * 可根据批量消费消息个数设置
     */
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            4,
            8,
            10,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(2048),
            new ThreadPoolExecutor.AbortPolicy());

    public static void main(String[] args) {
        new TheadPoolUtilTest().testPool();
    }

    public void testPool() {
        List<String> list = new ArrayList<>();
        list.add("order01");
        list.add("order02");
        list.add("order03");
        list.add("order04");

        List<CallableVoid> tasks = new ArrayList<>();
        for (String msg : list) {
            tasks.add(() -> doSomething(msg));
        }
        ThreadPoolUtil.invokeAllVoid(tasks);
    }

    private void doSomething(String msg) {
        try {
            // api call
//            TimeUnit.MILLISECONDS.sleep(20);

            if ("order03".equals(msg)) {
//            throw new RuntimeException("call api error!");
                for (int i = 0; i < 200000; i++) {
//                    TimeUnit.SECONDS.sleep(1);
                    System.out.println(Thread.currentThread().getName() + " ::03 isAlive  " + Thread.currentThread().isAlive());
                    System.out.println(Thread.currentThread().getName() + " ::03 isInterrupted " + Thread.currentThread().isInterrupted());
                    System.out.println(Thread.currentThread().getName() + " ::03 getState " + Thread.currentThread().getState());
                }
//            Thread.currentThread().interrupt();
            }

            if ("order04".equals(msg)) {
//            throw new RuntimeException("call api error!");
                for (int i = 0; i < 200000; i++) {
//                    TimeUnit.SECONDS.sleep(1);
                    System.out.println(Thread.currentThread().getName() + " ::::04 isAlive " + Thread.currentThread().isAlive());
                    System.out.println(Thread.currentThread().getName() + " ::::04 isInterrupted " + Thread.currentThread().isInterrupted());
                    System.out.println(Thread.currentThread().getName() + " ::::04 getState " + Thread.currentThread().getState());
                }
//            Thread.currentThread().interrupt();
            }

            System.out.println(msg);
        }
//        catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        finally {

        }
    }

}
