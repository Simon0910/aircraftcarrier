package com.aircraftcarrier.framework.exception;

/**
 * @author lzp
 */
public class LockNotAcquiredException extends Exception {

    public LockNotAcquiredException(String message) {
        super(message);
    }

    public LockNotAcquiredException(Throwable throwable) {
        super(throwable);
    }
}
