package com.aircraftcarrier.framework.tookit;

import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.framework.support.trace.TraceIdUtil;
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

    private ResponseWriterUtil() {
    }

    public static void responseMessage(String code, String msg, String detailMessage, HttpServletResponse response) {
        log.error("code : {}, msg : {}, detailMessage: {}", code, msg, detailMessage);

        response.setHeader("Content-type", "application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        SingleResponse<Object> singleResponse = SingleResponse.error(code, msg, detailMessage);
        singleResponse.setResponseId(TraceIdUtil.getTraceId());

        try (PrintWriter pw = response.getWriter()) {
            pw.print(JSON.toJSON(singleResponse));
            pw.flush();
        } catch (IOException ex) {
            log.error("统一异常处理 error", ex);
        }
    }

}
