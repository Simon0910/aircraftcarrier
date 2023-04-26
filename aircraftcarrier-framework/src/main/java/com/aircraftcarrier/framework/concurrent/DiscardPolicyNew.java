package com.aircraftcarrier.framework.concurrent;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author lzp
 * {@link ThreadPoolExecutor.DiscardPolicy}
 */
@Slf4j
public class DiscardPolicyNew implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (executor.isShutdown()) {
            return;
        }

        if (r instanceof Future) {
            // 丢弃Future
            log.info("DiscardPolicyNew ...");
            ((Future<?>) r).cancel(true);
        }
    }

}