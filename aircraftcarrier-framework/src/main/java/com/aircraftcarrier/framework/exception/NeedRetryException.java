package com.aircraftcarrier.framework.exception;

/**
 * 需要重试异常
 *
 * @author zhipengliu
 * @date 2024/7/6
 * @since 1.0
 */
public class NeedRetryException extends BaseException {
    private static final long serialVersionUID = 1L;

    public NeedRetryException(String errMessage) {
        super(ErrorCode.BIZ, errMessage);
    }

    public NeedRetryException(String errCode, String errMessage) {
        super(errCode, errMessage);
    }

    public NeedRetryException(String errMessage, Throwable e) {
        super(ErrorCode.BIZ, errMessage, e);
    }

    public NeedRetryException(String errorCode, String errMessage, Throwable e) {
        super(errorCode, errMessage, e);
    }

}