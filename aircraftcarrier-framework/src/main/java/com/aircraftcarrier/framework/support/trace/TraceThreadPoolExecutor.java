package com.aircraftcarrier.framework.support.trace;

import com.aircraftcarrier.framework.tookit.StringPool;
import com.aircraftcarrier.framework.tookit.StringUtil;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * TraceThreadPoolExecutor 重写原生线程池(含:传递MDC上下文)
 * 可以提交jdk原生的Runnable
 *
 * @author liuzhipeng
 */
public class TraceThreadPoolExecutor extends ThreadPoolExecutor {
    public TraceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public TraceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public TraceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public TraceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    public void execute(Runnable command) {
        // 提交者的本地变量
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        super.execute(() -> {
            if (contextMap != null) {
                // 如果提交者有本地变量, 任务执行之前放入当前任务所在的线程的本地变量中
                String traceId = contextMap.get(TraceIdUtil.TRACE_ID);
                contextMap.put(TraceIdUtil.TRACE_ID, StringUtil.append(traceId, TraceIdUtil.generatorTraceId(), StringPool.UNDERSCORE));
                MDC.setContextMap(contextMap);
            } else {
                Map<String, String> newContextMap = new HashMap<>(16);
                newContextMap.put(TraceIdUtil.TRACE_ID, TraceIdUtil.generatorTraceId());
                MDC.setContextMap(newContextMap);
            }
            try {
                command.run();
            } finally {
                // 任务执行完, 清除本地变量, 以防对后续任务有影响
                MDC.clear();
            }
        });
    }
}