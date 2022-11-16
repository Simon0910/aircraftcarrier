package com.aircraftcarrier.framework.exception;

/**
 * @author lzp
 */
public class LockNotAcquiredException extends RuntimeException {

    public LockNotAcquiredException(String message) {
        super(message);
    }

    public LockNotAcquiredException(Throwable throwable) {
        super(throwable);
    }
}
