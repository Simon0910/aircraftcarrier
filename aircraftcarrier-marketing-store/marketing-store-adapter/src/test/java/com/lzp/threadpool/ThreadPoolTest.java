package com.lzp.threadpool;

import com.aircraftcarrier.framework.tookit.ThreadPoolUtil;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 类注释内容
 *
 * @author zhipengliu
 * @date 2022/8/9
 * @since 1.0
 */
public class ThreadPoolTest {

    /**
     * 只需要一个线程去刷新，多余的请求丢弃忽略
     */
    private static final ExecutorService THREAD_POOL = ThreadPoolUtil.newCachedThreadPool(1, "accessToken");

    @Test
    public void synchronousQueueTest() throws InterruptedException {
        Executors.newSingleThreadExecutor();
        Executors.newFixedThreadPool(1);
        Executors.newCachedThreadPool();
        Executors.newScheduledThreadPool(1);

        for (int i = 0; i < 100; i++) {
            THREAD_POOL.execute(() -> {
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
            THREAD_POOL.execute(() -> {
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
