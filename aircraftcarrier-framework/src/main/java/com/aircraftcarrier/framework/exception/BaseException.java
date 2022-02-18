package com.aircraftcarrier.framework.exception;

/**
 * Base Exception is the parent of all exceptions
 *
 * @author fulan.zjf 2017年10月22日 上午12:00:39
 */
public class BaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    protected final int errCode;
    protected final String errMessage;

    protected BaseException(int errCode, String errMessage) {
        super(errMessage);
        this.errCode = errCode;
        this.errMessage = errMessage;
    }


    protected BaseException(int errCode, String errMessage, Throwable e) {
        super(errMessage, e);
        this.errCode = errCode;
        this.errMessage = errMessage;
    }

    public int getErrCode() {
        return errCode;
    }

    public String getErrMessage() {
        return errMessage;
    }
}
