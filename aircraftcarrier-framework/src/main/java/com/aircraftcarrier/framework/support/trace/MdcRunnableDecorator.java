package com.aircraftcarrier.framework.support.trace;

import org.slf4j.MDC;

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
    private final Map<String, String> mainMdcMap;
    private final Runnable runnable;

    public MdcRunnableDecorator(Runnable runnable) {
        this.runnable = runnable;
        this.mainMdcMap = MDC.getCopyOfContextMap();
    }

    @Override
    public void run() {
        // 将父线程的MDC值赋给子线程
        if (mainMdcMap != null) {
            MDC.setContextMap(mainMdcMap);
        }
        try {
            // 执行被装饰的线程run方法
            runnable.run();
        } finally {
            // 执行结束移除MDC值
            MDC.clear();
        }
    }

}