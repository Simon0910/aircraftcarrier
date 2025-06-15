package com.aircraftcarrier.framework.exception;


/**
 * @author lzp
 */
public class ToolException extends BaseException {

    private static final long serialVersionUID = 1L;

    public ToolException(String errMessage, Throwable e) {
        this(ErrorCode.SYS, errMessage, e);
    }

    public ToolException(String errCode, String errMessage) {
        super(errCode, errMessage);
    }

    public ToolException(String errCode, String errMessage, Throwable e) {
        super(errCode, errMessage, e);
    }
}