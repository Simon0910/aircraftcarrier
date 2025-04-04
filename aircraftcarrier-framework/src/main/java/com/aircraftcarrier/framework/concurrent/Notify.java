package com.aircraftcarrier.framework.concurrent;

/**
 * Notify
 *
 * @author zhipengliu
 * @date 2025/4/4
 * @since 1.0
 */
@FunctionalInterface
public interface Notify {
    /**
     * notify
     *
     * @param message message
     */
    void notify(String message);
}
