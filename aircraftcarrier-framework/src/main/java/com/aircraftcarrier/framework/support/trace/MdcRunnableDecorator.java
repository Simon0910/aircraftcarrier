package com.aircraftcarrier.framework.support.trace;

import com.aircraftcarrier.framework.tookit.StringPool;
import com.aircraftcarrier.framework.tookit.StringUtil;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

/**
 * 装饰器模式装饰Runnable，传递父线程的线程号
 * { @link https://www.cnblogs.com/yangyongjie/p/12523567.html }
 *
 * <p>
 * 想要在子线程获取主线程traceId, 可以使用此种方式
 * <pre> {@code
 *
 *  new Thread(new MdcRunnable(() -> {
 *      log.info("获取主线程的MDC上下文,例如traceId");
 *  })).start();
 *
 * }</pre>
 * </p>
 * <p>
 * MdcRunnableDecorator(含:传递MDC上下文)
 * jdk原生的线程池可以提交该任务
 *
 * @author lzp
 * @since 2021-12-2
 */
public class MdcRunnableDecorator implements Runnable {

    /**
     * 保存当前主线程的MDC值
     */
    private final Map<String, String> parentMdcMap;
    private final Runnable runnable;

    public MdcRunnableDecorator(Runnable runnable) {
        this.runnable = runnable;
        Map<String, String> parentContext = MDC.getCopyOfContextMap();
        if (parentContext == null) {
            parentContext = new HashMap<>();
        }
        this.parentMdcMap = parentContext;
    }

    @Override
    public void run() {
        // 传递context
        Map<String, String> curMdcMap = MDC.getCopyOfContextMap();
        if (curMdcMap == null) {
            curMdcMap = new HashMap<>();
        }
        curMdcMap.putAll(parentMdcMap);

        // 传递traceId
        String traceId = parentMdcMap.get(TraceIdUtil.TRACE_ID);
        if (traceId != null) {
            String[] parentTraceId = traceId.split(StringPool.UNDERSCORE);
            traceId = StringUtil.append(parentTraceId[parentTraceId.length - 1], TraceIdUtil.genUuid(), StringPool.UNDERSCORE);
        } else {
            traceId = TraceIdUtil.genUuid();
        }
        curMdcMap.put(TraceIdUtil.TRACE_ID, traceId);

        try {
            runnable.run();
        } finally {
            // 任务执行完, 清除本地变量, 以防对后续任务有影响
            MDC.clear();
        }
    }

}