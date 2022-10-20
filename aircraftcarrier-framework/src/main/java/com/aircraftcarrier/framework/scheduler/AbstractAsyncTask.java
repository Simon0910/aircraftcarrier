package com.aircraftcarrier.framework.scheduler;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * @author liuzhipeng
 */
@Slf4j
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
        // 1. 前者通知
        if (!before()) {
            log.info("task is not start");
        }

        boolean success = true;
        try {
            // 2. 任务执行
            runTask();
        } catch (Throwable e) {
            success = false;
            // 3. 异常通知
            afterThrowing(e);
            throw e;
        } finally {
            try {
                if (success) {
                    // 3. 后置通知
                    afterReturning();
                }
            } finally {
                // 4. 最终通知
                after();
            }
        }
    }

    /**
     * before
     *
     * @return boolean
     */
    public boolean before() {
        return true;
    }

    /**
     * runTask
     */
    public abstract void runTask();

    /**
     * afterReturn
     */
    public void afterReturning() {
    }

    /**
     * afterThrowing
     */
    public void afterThrowing(Throwable e) {
    }

    /**
     * after
     */
    public void after() {
    }
}