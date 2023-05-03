package com.aircraftcarrier.framework.exception;


/**
 * BizException is known Exception, no need retry
 *
 * @author Frank Zhang
 */
public class BizException extends BaseException {
    private static final long serialVersionUID = 1L;

    public BizException(String errMessage) {
        super(ErrorCode.BIZ, errMessage);
    }

    public BizException(String errCode, String errMessage) {
        super(errCode, errMessage);
    }

    public BizException(String errMessage, Throwable e) {
        super(ErrorCode.BIZ, errMessage, e);
    }

    public BizException(String errorCode, String errMessage, Throwable e) {
        super(errorCode, errMessage, e);
    }

}