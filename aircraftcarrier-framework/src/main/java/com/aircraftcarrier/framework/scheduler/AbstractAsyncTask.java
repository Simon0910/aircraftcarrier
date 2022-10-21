package com.aircraftcarrier.framework.scheduler;

import com.aircraftcarrier.framework.cache.LockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liuzhipeng
 */
@Slf4j
public abstract class AbstractAsyncTask implements Runnable {

    /**
     * state
     */
    private State state;

    private Map<String, AbstractAsyncTask> waitingTask = new ConcurrentHashMap<>();
    private Map<String, AbstractAsyncTask> runningTask = new ConcurrentHashMap<>();

    private String taskName;

    private String cron;

    public AbstractAsyncTask(String taskName, String cron) {
        Assert.hasText(taskName, "taskName must not be blank");
        Assert.hasText(cron, "cron must not be blank");
        this.taskName = taskName;
        this.cron = cron;
        this.state = State.WAITING;
    }

    @Override
    public final void run() {
        try {
            // 启动前被中断了
            if (Thread.currentThread().isInterrupted()) {
                log.error("i am isInterrupted so not run!");
                throw new RuntimeException("i am isInterrupted so not run!");
            }

            // 任务执行前获取分布式锁， 保证一个任务执行 （注意：各个环境不要争抢同一个锁影响）
            if (!LockUtil.tryLock(getTaskName())) {
                log.info("task get lock fail");
                return;
            }

            // 正常执行，waiting ==》 running
            synchronized (this) {
                state = State.RUNNING;
                removeWaiting();
                putRunning();
            }

            boolean success = true;
            try {
                // 1. 前者通知
                if (!before()) {
                    log.info("task before ==> false");
                    return;
                }

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

        } finally {
            // 一个短暂的 finally 状态
            state = State.FINALLY;
            // ********************中断通知*************************
            try {
                if (Thread.currentThread().isInterrupted()) {
                    // 可能还在运行集合，或等待集合，等待断中完成
                    // FINALLY RUNNING WAITING
                    interrupted();
                }
            } finally {
                // 正常执行，running ==》 waiting
                synchronized (this) {
                    state = State.WAITING;
                    putWaiting();
                    removeRunning();
                }

                if (Thread.currentThread().isInterrupted()) {
                    // 断中完成，移除等待集合
                    // INTERRUPTED
                    state = State.INTERRUPTED;
                    removeWaiting();
                }
                // ********************中断通知*************************

                // 释放锁
                LockUtil.unLock(getTaskName());
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

    /**
     * interrupted 任务
     */
    public void interrupted() {
    }

    public final String getTaskName() {
        return taskName;
    }

    public final String getCron() {
        return cron;
    }

    public final State getState() {
        return state;
    }

    public final boolean isRunning() {
        return state == State.RUNNING;
    }

    public final boolean isInterrupted() {
        return state == State.INTERRUPTED;
    }

    public final void setWaitingTask(Map<String, AbstractAsyncTask> waitingTask) {
        this.waitingTask = waitingTask;
    }

    public final void setRunningTask(Map<String, AbstractAsyncTask> runningTask) {
        this.runningTask = runningTask;
    }

    private void putWaiting() {
        waitingTask.put(taskName, this);
    }

    private void removeWaiting() {
        waitingTask.remove(taskName);
    }

    private void putRunning() {
        runningTask.put(taskName, this);
    }

    private void removeRunning() {
        runningTask.remove(taskName);
    }

//    public void removeRunning(AbstractAsyncTask task) {
//        runningTask.remove(task.getTaskName());
//    }

    enum State {
        WAITING, RUNNING, FINALLY, INTERRUPTED, TERMINATED
    }

    @Override
    public String toString() {
        return "AbstractAsyncTask{" + "taskName='" + taskName + '\'' + ", cron='" + cron + '\'' + ", state=" + state + '}';
    }
}