package com.aircraftcarrier.framework.scheduling;

import com.aircraftcarrier.framework.cache.LockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * @author liuzhipeng
 */
@Slf4j
public abstract class AbstractAsyncTask implements Runnable {

    /**
     * state
     * volatile 为了定时线程和手动线程相互及时的看到
     */
    private volatile State state;

    private Map<String, AbstractAsyncTask> dynamicTaskMap;

    private String taskName;

    private String cron;

    public AbstractAsyncTask(String taskName, String cron) {
        Assert.hasText(taskName, "taskName must not be blank");
        Assert.hasText(cron, "cron must not be blank");
        this.taskName = taskName;
        this.cron = cron;
        state = State.WAITING;
    }

    @Override
    public final void run() {
        try {
            // 任务执行前获取分布式锁， 保证一个任务执行 （注意：各个环境不要争抢同一个锁影响）
            if (!LockUtil.tryLock(getTaskName())) {
                // 可能手动任务抢到了锁，定时任务被挤掉了，只能到一个周期了
                log.info("task get lock fail");
                return;
            }

            // 正常执行，waiting ==》 running
            updateState(State.RUNNING);

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
            // 正常执行，running ==》 waiting
            updateState(State.WAITING);

            if (Thread.currentThread().isInterrupted()) {
                // 断中完成，移除等待集合
                // INTERRUPTED
                updateState(State.INTERRUPTED);
                dynamicTaskMap.remove(taskName);
            }

            // 释放锁
            // 失败了怎么办？register时会判断!schedule.isDone() 所以不会重复注册
            // 直到下次可重入再次执行任务？下次执行还是同一个线程吗，不是的话LockUtil实现逻辑就不可重入了！！待验证
            LockUtil.unLock(getTaskName());
        }

    }

    private void updateState(State newState) {
        state = newState;
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

    public final String getTaskName() {
        return taskName;
    }

    public final String getCron() {
        return cron;
    }

    public final State getState() {
        return state;
    }

    public final boolean istWaiting() {
        return state == State.WAITING;
    }

    public final boolean isRunning() {
        return state == State.RUNNING;
    }

    public final boolean isInterrupted() {
        return state == State.INTERRUPTED;
    }

    public final void holdTaskMap(Map<String, AbstractAsyncTask> dynamicTaskMap) {
        this.dynamicTaskMap = dynamicTaskMap;
    }

//    public void removeTask(AbstractAsyncTask task) {
//        dynamicTaskMap.remove(task.getTaskName());
//    }

    enum State {
        WAITING, RUNNING, FINALLY, INTERRUPTED, TERMINATED
    }

    @Override
    public String toString() {
        return "AbstractAsyncTask{" + "taskName='" + taskName + '\'' + ", cron='" + cron + '\'' + ", state=" + state + '}';
    }
}