package com.aircraftcarrier.framework.support.filter;

import com.aircraftcarrier.framework.support.trace.TraceIdUtil;
import com.aircraftcarrier.framework.tookit.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * @author lzp
 * @since 2021-12-2
 */
@Slf4j
public class TraceIdFilter extends OncePerRequestFilter {

    private static String getTraceIdFromRequest(HttpServletRequest httpServletRequest) {
        String traceId = httpServletRequest.getParameter(TraceIdUtil.REQUEST_ID);
        if (traceId != null) {
            log.debug("getParameter requestId: {}", traceId);
            return traceId;
        }

        traceId = httpServletRequest.getHeader(TraceIdUtil.TRACE_ID);
        if (traceId != null) {
            log.debug("getHeader TraceId: {}", traceId);
            return traceId;
        }

        traceId = TraceIdUtil.uuid();
        log.debug("generatorTraceId: {}", traceId);
        return traceId;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        try {
            // 设置日志ID
            String traceId = TraceIdUtil.getTraceId();
            if (traceId == null) {
                String traceIdFromRequest = getTraceIdFromRequest(httpServletRequest);
                LogUtil.requestStartByTid(traceIdFromRequest);
                TraceIdUtil.setTraceId(traceIdFromRequest);
            }
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } catch (Throwable throwable) {
            log.error("TraceIdFilter Throwable: ", throwable);
            throw throwable;
        } finally {
            TraceIdUtil.removeAll();
            LogUtil.requestEnd();
        }
    }

}
