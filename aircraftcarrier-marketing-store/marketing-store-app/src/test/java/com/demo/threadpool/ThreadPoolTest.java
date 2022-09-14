package com.demo.threadpool;

import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 类注释内容
 *
 * @author zhipengliu
 * @date 2022/8/9
 * @since 1.0
 */
public class ThreadPoolTest {

    private static final ThreadPoolExecutor POOL = new ThreadPoolExecutor(0, 1, 0L, TimeUnit.MILLISECONDS,
            // 只需要一个线程去刷新，多余的请求丢弃忽略
            new SynchronousQueue<>(), new ThreadPoolExecutor.DiscardPolicy());

    @Test
    public void synchronousQueueTest() throws InterruptedException {
        Executors.newSingleThreadExecutor();
        Executors.newFixedThreadPool(1);
        Executors.newCachedThreadPool();
        Executors.newScheduledThreadPool(1);

        for (int i = 0; i < 100; i++) {
            POOL.execute(() -> {
                try {
                    System.out.println("start....");
                    Thread.sleep(5000);
                    System.out.println("finish");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        System.out.println("sleep....");
        Thread.sleep(8000);

        for (int i = 0; i < 100; i++) {
            POOL.execute(() -> {
                try {
                    System.out.println("start2....");
                    Thread.sleep(2000);
                    System.out.println("finish2");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        System.out.println("sleep....");
        Thread.sleep(3000);
    }
}
