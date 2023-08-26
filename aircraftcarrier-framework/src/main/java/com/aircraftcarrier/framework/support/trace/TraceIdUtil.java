package com.aircraftcarrier.framework.support.trace;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Map;
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
    public static final String FIXED_NAME = "fixedName";
    public static final String MODULE_NAME = "moduleName";

    private TraceIdUtil() {
    }

    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    public static void removeAll() {
        MDC.remove(TRACE_ID);
        MDC.remove(FIXED_NAME);
        MDC.remove(MODULE_NAME);
    }

    /**
     * 获取日志ID, 没有返回UUID
     *
     * @return traceId
     */
    public static String getTraceIdOrUuid() {
        String traceId = getTraceId();
        if (traceId == null) {
            traceId = uuid();
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
        setTraceId(uuid());
    }

    public static String uuid() {
        // return UUID.randomUUID().toString().replace("-", "");
        return String.valueOf(System.nanoTime());
    }

    /**
     * setFixedName
     */
    public static void setFixedName(String fixedName) {
        MDC.put(FIXED_NAME, fixedName);
    }

    /**
     * setModuleName
     */
    public static void setModuleName(String moduleName) {
        MDC.put(MODULE_NAME, moduleName);
    }

}
