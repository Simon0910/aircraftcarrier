package com.aircraftcarrier.framework.exception;


/**
 * @author lzp
 */
public class ToolException extends BaseException {

    private static final long serialVersionUID = 1L;

    public ToolException(int errCode, String errMessage) {
        super(errCode, errMessage);
    }

    public ToolException(int errCode, String errMessage, Throwable e) {
        super(errCode, errMessage, e);
    }
}