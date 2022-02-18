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
    public static String getTraceIdOrUUID() {
        String traceId = getTraceId();
        if (traceId == null) {
            traceId = generatorTraceId();
        }
        return traceId;
    }

    /**
     * 获取日志ID, 没有强制生产日志ID
     * RPC调用
     *
     * @return traceId
     */
    public static String getTraceIdOrSettingUUID() {
        String traceId = getTraceId();
        if (traceId == null) {
            setUUID();
        }
        return traceId;
    }

    /**
     * 强制生产日志ID
     * MQ消息, RPC接收
     *
     * @return traceId
     */
    public static String setUUID() {
        String traceId = generatorTraceId();
        setTraceId(traceId);
        return traceId;
    }

    public static String generatorTraceId() {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        log.info("generator traceId: 【{}】", traceId);
        return traceId;
    }
}
