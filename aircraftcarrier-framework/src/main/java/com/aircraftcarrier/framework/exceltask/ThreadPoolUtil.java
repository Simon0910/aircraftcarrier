package com.aircraftcarrier.framework.exceltask;

import com.aircraftcarrier.framework.concurrent.BlockPolicy;
import com.aircraftcarrier.framework.concurrent.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author zhipengliu
 */
@Slf4j
public class ThreadPoolUtil {

    private ThreadPoolUtil() {
    }

    public static NamedThreadFactory newThreadFactory(String poolName) {
        return new NamedThreadFactory(poolName);
    }

    public static BlockPolicy newBlockPolicy() {
        return new BlockPolicy();
    }

    /**
     * 获取 生成当前线程的编号
     *
     * @return String
     */
    public static String getThreadNo() {
        return Thread.currentThread().getName().substring(Thread.currentThread().getName().lastIndexOf("-") + 1);
    }

    /**
     * 睡眠 （毫秒）
     *
     * @param timeout timeout
     */
    public static void sleepMilliseconds(long timeout) throws InterruptedException {
        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    /**
     * 睡眠 （秒）
     *
     * @param timeout timeout
     */
    public static void sleepSeconds(long timeout) throws InterruptedException {
        try {
            TimeUnit.SECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

}
