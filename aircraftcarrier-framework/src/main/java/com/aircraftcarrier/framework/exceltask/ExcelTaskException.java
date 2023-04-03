package com.aircraftcarrier.framework.exceltask;

/**
 * @author zhipengliu
 */
public class ExcelTaskException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public final int errCode;
    public final String errMessage;

    public ExcelTaskException(String errMessage) {
        super(errMessage);
        this.errCode = 500;
        this.errMessage = errMessage;
    }

    public ExcelTaskException(int errCode, String errMessage) {
        super(errMessage);
        this.errCode = errCode;
        this.errMessage = errMessage;
    }


    public ExcelTaskException(int errCode, String errMessage, Exception e) {
        super(errMessage, e);
        this.errCode = errCode;
        this.errMessage = errMessage;
    }

    public ExcelTaskException(String errMessage, Exception e) {
        super(errMessage, e);
        this.errCode = 500;
        this.errMessage = errMessage;

    }

    public int getErrCode() {
        return errCode;
    }

    public String getErrMessage() {
        return errMessage;
    }
}