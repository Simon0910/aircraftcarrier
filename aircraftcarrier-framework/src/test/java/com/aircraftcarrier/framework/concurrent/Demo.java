package com.aircraftcarrier.framework.concurrent;

import com.aircraftcarrier.framework.tookit.SleepUtil;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class Demo {
    public static void main(String[] args) {
        FutureTask<Void> f = new FutureTask<>(() -> {
            System.out.println("123");
        }, null);

        while (!f.isDone()) {
            SleepUtil.sleepSeconds(1);
            /**
             * 配合
             * {@link DiscardPolicyNew#rejectedExecution(java.lang.Runnable, java.util.concurrent.ThreadPoolExecutor)}
             */
//            Future.State state = f.state();
//            System.out.println(state);
//            if (f.isCancelled()) {
//                break;
//            }
        }

    }
}
