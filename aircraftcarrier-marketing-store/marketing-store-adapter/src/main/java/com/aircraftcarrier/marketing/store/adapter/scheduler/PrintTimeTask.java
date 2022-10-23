package com.aircraftcarrier.marketing.store.adapter.scheduler;

import com.aircraftcarrier.framework.scheduling.DynamicTaskService;
import com.aircraftcarrier.framework.tookit.SleepUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liuzhipeng
 */
@Slf4j
public class PrintTimeTask extends DynamicTaskService.AbstractAsyncTask {

    private static final String TASK_NAME = "print";
    private static final String CRON = "0/60 * * * * ?";

    public PrintTimeTask() {
        this(CRON);
    }

    public PrintTimeTask(String cron) {
        this(TASK_NAME, cron);
    }

    public PrintTimeTask(String taskName, String cron) {
        super(taskName, cron);
    }

    @Override
    public void runTask() {
        for (int i = 0; i < 10; i++) {
            SleepUtil.sleepSeconds(1);
            log.info("i am running task:[{}] i = {} state = {}", getTaskName(), i, getState());

            // if isInterrupted to do something
            if (Thread.currentThread().isInterrupted()) {
                log.info("ok i am stop ! to do finish");
//                break;
                throw new RuntimeException("ok i am stop !");
            }
        }
    }

}
