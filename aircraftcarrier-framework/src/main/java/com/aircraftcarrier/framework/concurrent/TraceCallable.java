package com.aircraftcarrier.framework.concurrent;

import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * TraceCallable
 *
 * @author zhipengliu
 * @since 1.0
 */
public class TraceCallable<V> implements Callable<V> {

    /**
     * 保存当前主线程的MDC值
     */
    private final Map<String, String> parentMdcMap;

    private final Callable<V> callable;

    public TraceCallable(Callable<V> callable) {
        this.callable = callable;
        Map<String, String> parentContext = MDC.getCopyOfContextMap();
        if (parentContext == null) {
            parentContext = new HashMap<>();
        }
        this.parentMdcMap = parentContext;
    }

    @Override
    public V call() throws Exception {
        return TraceAvailableExecute.execute(parentMdcMap, () -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
