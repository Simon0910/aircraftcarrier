package com.aircraftcarrier.marketing.store.adapter.scheduler;

import com.aircraftcarrier.framework.cache.LockUtil;
import com.aircraftcarrier.framework.scheduler.AbstractAsyncTask;
import com.aircraftcarrier.framework.tookit.DateTimeUtil;
import com.aircraftcarrier.framework.tookit.SleepUtil;

/**
 * @author liuzhipeng
 */
public class PrintTimeTask extends AbstractAsyncTask {

    public PrintTimeTask() {
        this("0/10 * * * * ?");
    }

    public PrintTimeTask(String cron) {
        super("print", cron);
    }

    @Override
    public boolean before() {
        // if task running return false;
        // else 获取锁
        if (LockUtil.tryLock(getTaskName())) {
            // if task running return false;
            // 保存数据库标识 task_running
            System.out.println("task_running");
            // unlock(getTaskName())
            return true;
        }
        return false;
    }

    @Override
    public void runTask() {
        for (int i = 0; i < 10; i++) {
            SleepUtil.sleepSeconds(1);
            System.out.println("i am running: " + DateTimeUtil.now());
            if (Thread.currentThread().isInterrupted()) {
//                System.out.println("ok i am stop !");
//                break;
            }
        }
    }

    @Override
    public void afterReturning() {
        // 保存数据库标识 task_success
        System.out.println("task_success");
    }

    @Override
    public void afterThrowing(Throwable e) {
        // 保存数据库标识 task_fail
        System.out.println("task_fail");
    }
}
