package com.aircraftcarrier.marketing.store.adapter.scheduler;

import com.aircraftcarrier.framework.scheduler.AbstractAsyncTask;
import com.aircraftcarrier.framework.tookit.DateTimeUtil;
import com.aircraftcarrier.framework.tookit.SleepUtil;

/**
 * @author liuzhipeng
 */
public class PrintTimeTask extends AbstractAsyncTask {

    public PrintTimeTask() {
        this("0/60 * * * * ?");
    }

    public PrintTimeTask(String cron) {
        super("print", cron);
    }

    @Override
    public void runTask() {
        for (int i = 0; i < 10; i++) {
            SleepUtil.sleepSeconds(1);
            System.out.println("i am running " + i + ": " + DateTimeUtil.now());

            System.out.println("state:: " + getState());

            // if isInterrupted to do something
            if (Thread.currentThread().isInterrupted()) {
                System.out.println("ok i am stop ! to do finish");
                break;
//                throw new RuntimeException("ok i am stop !");
            }
        }
    }

}
