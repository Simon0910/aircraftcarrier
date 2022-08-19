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
            ThreadPoolUtil.CORE_POOL_SIZE,
            ThreadPoolUtil.MAX_POOL_SIZE,
            ThreadPoolUtil.KEEP_ALIVE_TIME,
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

        List<CallableVoid> tasks = new ArrayList<>();
        for (String msg : list) {
            tasks.add(() -> doSomething(msg));
        }
        ThreadPoolUtil.executeAllVoid(THREAD_POOL_EXECUTOR, tasks);
    }

    private void doSomething(String msg) throws RuntimeException {
        try {
            // api call
            Thread.sleep(2000);
        } catch (Exception e) {
            log.error("xxx: ", e);
        }

        if ("order03".equals(msg)) {
            throw new RuntimeException("call api error!");
        }

    }

}
