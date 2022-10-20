package com.aircraftcarrier.framework.scheduler;

import com.aircraftcarrier.framework.cache.LockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
    private WeakReference<Map<String, AbstractAsyncTask>> waitingTask = new WeakReference<>(new ConcurrentHashMap<>());
    private WeakReference<Map<String, AbstractAsyncTask>> runningTask = new WeakReference<>(new ConcurrentHashMap<>());

    private String taskName;

    private String cron;

    public AbstractAsyncTask(String taskName, String cron) {
        Assert.hasText(taskName, "taskName must not be blank");
        Assert.hasText(cron, "cron must not be blank");
        this.taskName = taskName;
        this.cron = cron;
    }

    @Override
    public final void run() {
        if (Thread.currentThread().isInterrupted()) {
            log.error("i am isInterrupted so not run!");
            throw new RuntimeException("i am isInterrupted so not run!");
        }

        try {
            // 1. 前者通知
            if (!before()) {
                log.info("task before ==> false");
                return;
            }

            synchronized (this) {
                state = State.RUNNING;
                Optional.ofNullable(waitingTask).flatMap(e -> Optional.ofNullable(e.get())).ifPresent(t -> t.remove(taskName));
                Optional.ofNullable(runningTask).flatMap(e -> Optional.ofNullable(e.get())).ifPresent(t -> t.put(taskName, this));
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
            state = State.FINALLY;
            synchronized (this) {
                Optional.ofNullable(waitingTask).flatMap(e -> Optional.ofNullable(e.get())).ifPresent(t -> t.put(taskName, this));
                Optional.ofNullable(runningTask).flatMap(e -> Optional.ofNullable(e.get())).ifPresent(t -> t.remove(taskName));
            }
            if (Thread.currentThread().isInterrupted()) {
                state = State.INTERRUPTED;
                interrupted();
            }
            LockUtil.unLock(getTaskName());
        }

    }

    /**
     * before
     *
     * @return boolean
     */
    public boolean before() {
        if (LockUtil.tryLock(getTaskName())) {
            log.info("task get lock ok");
            return true;
        }
        log.info("task get lock fail");
        return false;
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

    public final void setState(State state) {
        this.state = state;
    }

    public final boolean isRunning() {
        return state == State.RUNNING;
    }

    public final boolean isInterrupted() {
        return state == State.INTERRUPTED;
    }

    public final Map<String, AbstractAsyncTask> getWaitingTask() {
        return Optional.ofNullable(waitingTask).flatMap(e -> Optional.ofNullable(e.get())).orElse(new HashMap<>());
    }

    public final void setWaitingTask(Map<String, AbstractAsyncTask> waitingTask) {
        this.waitingTask = new WeakReference<>(waitingTask);
    }

    public final Map<String, AbstractAsyncTask> getRunningTask() {
        return Optional.ofNullable(runningTask).flatMap(e -> Optional.ofNullable(e.get())).orElse(new HashMap<>());
    }

    public final void setRunningTask(Map<String, AbstractAsyncTask> runningTask) {
        this.runningTask = new WeakReference<>(runningTask);
    }

    enum State {
        WAITING, RUNNING, FINALLY, INTERRUPTED, TERMINATED
    }

}