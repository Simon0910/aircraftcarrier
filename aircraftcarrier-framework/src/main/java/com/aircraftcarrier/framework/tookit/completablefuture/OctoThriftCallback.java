package com.aircraftcarrier.framework.tookit.completablefuture;

/**
 * @author meituan
 */
public interface OctoThriftCallback<O, T> {

    /**
     * addObserver
     *
     * @param tOctoObserver tOctoObserver
     */
    void addObserver(OctoObserver<T> tOctoObserver);
}
