package com.aircraftcarrier.framework.concurrent;


import java.util.concurrent.TimeUnit;

/**
 * 类注释内容
 *
 * @author zhipengliu
 * @date 2022/10/15
 * @since 1.0
 */
public class BusyUtil {

    public static void busyFor(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
