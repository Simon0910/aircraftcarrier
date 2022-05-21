//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.aircraftcarrier.marketing.store.adapter.config;

import com.aircraftcarrier.framework.exception.BizException;
import com.aircraftcarrier.framework.exception.ErrorCode;
import com.aircraftcarrier.framework.exception.FrameworkException;
import com.aircraftcarrier.framework.exception.SysException;
import com.aircraftcarrier.framework.tookit.ResponseWriterUtil;
import com.alibaba.excel.exception.ExcelAnalysisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;
import java.util.List;

/**
 * @author lzp
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    private GlobalExceptionHandler() {
    }

    @ExceptionHandler({HttpMediaTypeNotSupportedException.class, HttpRequestMethodNotSupportedException.class})
    public void httpHandler(ServletException e, HttpServletResponse response) {
        this.doHandle(ErrorCode.PARAMS_GET_ERROR, e.getMessage(), response);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    public void httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException e, HttpServletResponse response) {
        ResponseWriterUtil.handlerExceptionMessage(ErrorCode.PARAMS_GET_ERROR, e.getMessage(), response);
    }

    @ExceptionHandler({BizException.class})
    public void bizExceptionHandler(BizException e, HttpServletResponse response) {
        this.doHandle(e.getErrCode(), e.getErrMessage(), response);
    }

    @ExceptionHandler({FrameworkException.class})
    public void frameworkExceptionHandler(FrameworkException e, HttpServletResponse response) {
        this.doHandle(e.getErrCode(), e.getErrMessage(), response);
    }

    @ExceptionHandler({BindException.class})
    public void bindExceptionHandler(BindException e, HttpServletResponse response) {
        this.doHandle(ErrorCode.PARAMS_GET_ERROR, "", response);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public void methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e, HttpServletResponse response) {
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        StringBuilder sb = new StringBuilder();
        for (Iterator<FieldError> iterator = fieldErrors.iterator(); iterator.hasNext(); ) {
            FieldError fieldError = iterator.next();
            sb.append(fieldError.getField()).append(":").append(fieldError.getDefaultMessage());
            if (iterator.hasNext()) {
                sb.append("; ");
            }
        }
        ResponseWriterUtil.handlerExceptionMessage(ErrorCode.PARAMS_GET_ERROR, sb.toString(), response);
    }

    @ExceptionHandler({ExcelAnalysisException.class})
    public void excelAnalysisExceptionHandler(ExcelAnalysisException e, HttpServletResponse response) {
        log.error("内部系统错误: ", e);
        this.doHandle(ErrorCode.SYS, e.getMessage(), response);
    }

    @ExceptionHandler({SysException.class})
    public void sysExceptionHandler(SysException e, HttpServletResponse response) {
        log.error("内部系统错误: ", e);
        this.doHandle(ErrorCode.SYS, e.getErrMessage(), response);
    }

    @ExceptionHandler({Exception.class})
    public void exceptionHandler(Exception e, HttpServletResponse response) {
        log.error("内部系统错误: ", e);
        this.doHandle(ErrorCode.SYS, "内部系统错误", response);
    }

    private void doHandle(int errorCode, String errorMsg, HttpServletResponse response) {
        ResponseWriterUtil.handlerExceptionMessageI18n(errorCode, errorMsg, response);
    }
}
