package com.aircraftcarrier.framework.exception;

/**
 * 线程异常
 *
 * @author zhipengliu
 * @date 2022/8/29
 * @since 1.0
 */
public class ThreadException extends RuntimeException {
    public ThreadException(Throwable cause) {
        super(cause);
    }

    public ThreadException(String message) {
        super(message);
    }

    public ThreadException(String message, Throwable cause) {
        super(message, cause);
    }

}
