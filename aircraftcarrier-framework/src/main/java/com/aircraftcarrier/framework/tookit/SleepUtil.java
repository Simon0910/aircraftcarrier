package com.aircraftcarrier.framework.tookit;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author liuzhipeng
 */
@Slf4j
public class SleepUtil {

    public static void sleepSeconds(long timeout) {
        try {
            TimeUnit.SECONDS.sleep(timeout);
        } catch (InterruptedException ignore) {
            // I was interrupted
            Thread.currentThread().interrupt();
            log.info("{} was interrupted", Thread.currentThread().getName());
        }
    }

    public static void sleepMilliseconds(long timeout) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (InterruptedException ignore) {
            // I was interrupted
            Thread.currentThread().interrupt();
            log.info("{} was interrupted", Thread.currentThread().getName());
        }
    }
}
