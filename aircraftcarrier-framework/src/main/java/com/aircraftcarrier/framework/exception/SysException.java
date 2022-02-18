package com.aircraftcarrier.framework.exception;

/**
 * System Exception is unexpected Exception, retry might work again
 *
 * @author Danny.Lee 2018/1/27
 */
public class SysException extends BaseException {
    private static final long serialVersionUID = 1L;

    public SysException(String errMessage) {
        super(ErrorCode.SYS, errMessage);
    }

    public SysException(int errCode, String errMessage) {
        super(errCode, errMessage);
    }

    public SysException(String errMessage, Throwable e) {
        super(ErrorCode.SYS, errMessage, e);
    }

    public SysException(int errorCode, String errMessage, Throwable e) {
        super(errorCode, errMessage, e);
    }

}
