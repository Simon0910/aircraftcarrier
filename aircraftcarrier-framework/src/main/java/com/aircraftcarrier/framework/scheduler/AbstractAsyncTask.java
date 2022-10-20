package com.aircraftcarrier.framework.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * @author liuzhipeng
 */
@Slf4j
public abstract class AbstractAsyncTask implements Runnable {

    /**
     * state
     */
    private State state;
    private WeakReference<Map<String, AbstractAsyncTask>> waitingTask;
    private WeakReference<Map<String, AbstractAsyncTask>> runningTask;

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
        try {
            synchronized (this) {
                state = State.RUNNING;
                if (waitingTask != null) {
                    waitingTask.get().remove(taskName);
                }
                if (runningTask != null) {
                    runningTask.get().put(taskName, this);
                }
            }

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

        } finally {
            setState(State.FINALLY);
            if (Thread.currentThread().isInterrupted()) {
                setState(State.INTERRUPTED);
            }
            synchronized (this) {
                state = State.WAITING;
                if (waitingTask != null) {
                    waitingTask.get().put(taskName, this);
                }
                if (runningTask != null) {
                    runningTask.get().remove(taskName);
                }
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
     * stop
     */
    public void stop() {

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

    public final void setState(State state) {
        this.state = state;
    }

    public final boolean isRunning() {
        return State.RUNNING == getState();
    }

    public final boolean isInterrupted() {
        return State.INTERRUPTED == getState();
    }

    public final Map<String, AbstractAsyncTask> getWaitingTask() {
        return waitingTask.get();
    }

    public final void setWaitingTask(Map<String, AbstractAsyncTask> waitingTask) {
        this.waitingTask = new WeakReference<>(waitingTask);
    }

    public final Map<String, AbstractAsyncTask> getRunningTask() {
        return runningTask.get();
    }

    public final void setRunningTask(Map<String, AbstractAsyncTask> runningTask) {
        this.runningTask = new WeakReference<>(runningTask);
    }

    enum State {
        WAITING, RUNNING, INTERRUPTED, FINALLY, TERMINATED
    }

}