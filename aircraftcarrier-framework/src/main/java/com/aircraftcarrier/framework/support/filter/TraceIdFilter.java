package com.aircraftcarrier.framework.support.filter;

import com.aircraftcarrier.framework.support.trace.TraceIdUtil;
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

        traceId = TraceIdUtil.genUuid();
        log.debug("generatorTraceId: {}", traceId);
        return traceId;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        // 设置日志ID
        String traceId = TraceIdUtil.getTraceId();
        if (traceId == null) {
            TraceIdUtil.setTraceId(getTraceIdFromRequest(httpServletRequest));
        }

        try {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } catch (Throwable throwable) {
            log.error("TraceIdFilter Throwable: ", throwable);
            throw throwable;
        } finally {
            TraceIdUtil.removeTraceId();
        }
    }
}
