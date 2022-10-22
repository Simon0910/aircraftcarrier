package com.aircraftcarrier.framework.concurrent;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author lzp
 */
@Slf4j
public class MyDiscardPolicyRejectedExecutionHandler implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        if (!e.isShutdown()) {
            if (r instanceof FutureTask) {
                log.error("My DiscardPolicy ...");
                ((FutureTask<?>) r).cancel(true);
            }
        }
    }

}