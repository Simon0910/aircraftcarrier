package com.aircraftcarrier.framework.exception;


/**
 * @author lzp
 */
public class FrameworkException extends BaseException {

    private static final long serialVersionUID = 1L;

    public FrameworkException(String errCode, String errMessage) {
        super(errCode, errMessage);
    }

    public FrameworkException(String errCode, String errMessage, Throwable e) {
        super(errCode, errMessage, e);
    }

    public FrameworkException(String errMessage) {
        super(ErrorCode.INTERNAL_SERVER_ERROR, errMessage);
    }
}