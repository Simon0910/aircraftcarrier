package com.aircraftcarrier.framework.scheduler;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.Assert;

/**
 * @author liuzhipeng
 */
@Data
@Accessors(chain = true)
public abstract class AbstractAsyncTask implements Runnable {

    private String taskName;

    private String cron;

    public AbstractAsyncTask(String taskName, String cron) {
        Assert.hasText(taskName, "taskName must not be blank");
        Assert.hasText(cron, "cron must not be blank");
        this.taskName = taskName;
        this.cron = cron;
    }

    @Override
    public void run() {
        runTask();
    }

    /**
     * runTask
     */
    public abstract void runTask();
}