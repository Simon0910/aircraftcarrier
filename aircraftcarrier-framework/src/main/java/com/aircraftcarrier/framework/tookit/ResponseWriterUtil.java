package com.aircraftcarrier.framework.tookit;

import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author lzp
 */
@Slf4j
public class ResponseWriterUtil {

    public static void handlerExceptionMessage(int errorCode, String errorMsg, HttpServletResponse response) {
        log.error("errorCode : {}, errorMsg : {}", errorCode, errorMsg);

        response.setHeader("Content-type", "application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter pw = response.getWriter()) {
            pw.print(JSON.toJSON(SingleResponse.error(errorCode, errorMsg)));
            pw.flush();
        } catch (IOException ex) {
            log.error("统一异常处理 error", ex);
        }
    }

    public static void handlerExceptionMessageI18n(int errorCode, String errorMsg, HttpServletResponse response) {
        log.error("errorCode : {}, errorMsg : {}", errorCode, errorMsg);

        response.setHeader("Content-type", "application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter pw = response.getWriter()) {
            pw.print(JSON.toJSON(SingleResponse.error(errorCode)));
            pw.flush();
        } catch (IOException ex) {
            log.error("统一异常处理 error", ex);
        }
    }
}
