package com.aircraftcarrier.framework.tookit.completablefuture;

/**
 * @author meituan
 */
public interface OctoObserver<T> {

    /**
     * onSuccess
     *
     * @param t t
     */
    void onSuccess(T t);

    /**
     * onFailure
     *
     * @param throwable throwable
     */
    void onFailure(Throwable throwable);
}
