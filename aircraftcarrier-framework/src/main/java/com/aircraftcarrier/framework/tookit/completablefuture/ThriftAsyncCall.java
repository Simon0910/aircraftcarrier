package com.aircraftcarrier.framework.tookit.completablefuture;

/**
 * @author meituan
 */
@FunctionalInterface
public interface ThriftAsyncCall {
    /**
     * invoke
     *
     * @throws TException e
     */
    void invoke() throws TException;
}