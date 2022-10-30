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
        this.parentMdcMap = MDC.getCopyOfContextMap();
    }

    @Override
    public void run() {
        if (parentMdcMap != null) {
            // 如果提交者有本地变量, 任务执行之前放入当前任务所在的线程的本地变量中
            String traceId = parentMdcMap.get(TraceIdUtil.TRACE_ID);
            parentMdcMap.put(TraceIdUtil.TRACE_ID, StringUtil.append(traceId, TraceIdUtil.genUuid(), StringPool.UNDERSCORE));
            MDC.setContextMap(parentMdcMap);
        } else {
            Map<String, String> newContextMap = new HashMap<>(16);
            newContextMap.put(TraceIdUtil.TRACE_ID, TraceIdUtil.genUuid());
            MDC.setContextMap(newContextMap);
        }
        try {
            runnable.run();
        } finally {
            // 任务执行完, 清除本地变量, 以防对后续任务有影响
            MDC.clear();
        }
    }

}