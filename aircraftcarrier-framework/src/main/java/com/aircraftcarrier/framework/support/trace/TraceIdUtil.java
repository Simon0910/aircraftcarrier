package com.aircraftcarrier.framework.support.trace;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * TraceIdUtil
 *
 * @author lzp
 * @since 2021-12-06
 */
@Slf4j
public class TraceIdUtil {

    public static final String TRACE_ID = "traceId";

    public static final String REQUEST_ID = "requestId";

    private TraceIdUtil() {
    }

    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    public static void removeTraceId() {
        MDC.remove(TRACE_ID);
    }

    /**
     * 获取日志ID, 没有返回UUID
     *
     * @return traceId
     */
    public static String getTraceIdOrUuid() {
        String traceId = getTraceId();
        if (traceId == null) {
            traceId = genUuid();
        }
        return traceId;
    }

    /**
     * 获取日志ID, 没有强制生产日志ID
     * RPC调用
     *
     * @return traceId
     */
    public static String getTraceIdOrSetUuid() {
        String traceId = getTraceId();
        if (traceId != null) {
            return traceId;
        }
        setTraceIdByUuid();
        return getTraceId();
    }

    /**
     * 强制生产日志ID
     * MQ消息, RPC接收
     *
     * @return traceId
     */
    public static void setTraceIdByUuid() {
        setTraceId(genUuid());
    }

    public static String genUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
