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
public class DiscardPolicyRejectedExecutionHandler implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        if (!e.isShutdown()) {
            if (r instanceof Future) {
                log.error("My DiscardPolicy ...");
                ((Future<?>) r).cancel(true);
            }
        }
    }

}