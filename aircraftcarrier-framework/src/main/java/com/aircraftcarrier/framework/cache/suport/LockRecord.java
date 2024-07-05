package com.aircraftcarrier.framework.cache.suport;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Token
 *
 * @author zhipengliu
 * @date 2024/7/4
 * @since 1.0
 */
@Getter
@Setter
public class LockRecord {
    private String key;
    private String value;
    private Long expire;
    private Thread currentThread;
    private Long startTime;

    public LockRecord() {
        this.startTime = new Date().getTime();
        this.currentThread = Thread.currentThread();
    }
}
