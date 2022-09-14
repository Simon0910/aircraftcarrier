package com.aircraftcarrier.framework.concurrent;

/**
 * @author zhipengliu
 */
@FunctionalInterface
public interface CallableVoid {
    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @throws Exception if unable to compute a result
     */
    void call() throws Exception;
}
