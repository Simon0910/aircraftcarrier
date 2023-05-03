package com.aircraftcarrier.framework.concurrent;

import com.aircraftcarrier.framework.support.trace.TraceIdUtil;
import com.aircraftcarrier.framework.tookit.StringPool;
import com.aircraftcarrier.framework.tookit.StringUtil;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 类注释内容
 *
 * @author zhipengliu
 * @date 2023/5/4
 * @since 1.0
 */
public class TraceAvailableExecute {

    public static <T> T execute(Map<String, String> parentContext, Supplier<T> supplier) {
        // 传递context
        Map<String, String> curMdcMap = MDC.getCopyOfContextMap();
        if (curMdcMap == null) {
            curMdcMap = new HashMap<>();
        }
        curMdcMap.putAll(parentContext);

        // 传递traceId
        String traceId = parentContext.get(TraceIdUtil.TRACE_ID);
        if (traceId != null) {
            String[] parentTraceId = traceId.split(StringPool.UNDERSCORE);
            traceId = StringUtil.append(parentTraceId[parentTraceId.length - 1], String.valueOf(System.nanoTime()), StringPool.UNDERSCORE);
        } else {
            traceId = TraceIdUtil.genUuid();
        }
        curMdcMap.put(TraceIdUtil.TRACE_ID, traceId);

        // new ContextMap
        MDC.setContextMap(curMdcMap);

        try {
            return supplier.get();
        } finally {
            // 任务执行完, 清除本地变量, 以防对后续任务有影响
            MDC.clear();
        }
    }

}
