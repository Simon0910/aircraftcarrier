package com.aircraftcarrier.framework.concurrent;

import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author lzp
 */
public class MyDiscardPolicyRejectedExecutionHandler implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        if (!e.isShutdown()) {
            if (r instanceof FutureTask) {
                ((FutureTask<?>) r).cancel(true);
            }
        }
    }

}