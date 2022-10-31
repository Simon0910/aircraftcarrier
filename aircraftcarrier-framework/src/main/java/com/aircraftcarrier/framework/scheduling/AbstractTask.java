package com.aircraftcarrier.framework.scheduling;

import com.aircraftcarrier.framework.cache.LockUtil;
import com.aircraftcarrier.framework.tookit.SleepUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * @author liuzhipeng
 */
@Slf4j
public abstract class AbstractTask implements Runnable {

    private final String taskName;
    private final String cron;

    private final CronExpression cronExpression;

    /**
     * 延迟多久执行 毫秒
     */
    private final long delay;

    private LocalDateTime nextRuntime;

    /**
     * state
     * volatile 为了定时线程和手动线程相互及时的看到
     */
    private volatile State state;
    /**
     * 进度 0-100
     */
    private volatile int progress;
    private Map<String, AbstractTask> dynamicTaskMap;

    public AbstractTask(String taskName, String cron) {
        this(taskName, cron, 0);
    }

    public AbstractTask(String taskName, String cron, long delay) {
        Assert.hasText(taskName, "taskName must not be blank");
        Assert.hasText(cron, "cron must not be blank");
        Assert.isTrue(CronExpression.isValidExpression(cron), "An invalid corn expression");
        Assert.isTrue(delay >= 0, "delay must not be >= 0");
        this.taskName = taskName;
        this.cron = cron;
        this.cronExpression = CronExpression.parse(cron);
        this.delay = delay;
        this.state = State.WAITING;
        this.progress = 0;

        calculateNextRuntime();
    }

    private void calculateNextRuntime() {
        LocalDateTime nextTime = cronExpression.next(LocalDateTime.now());
        if (delay == 0) {
            nextRuntime = nextTime;
        } else {
            long milliSecond = nextTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
            long exeInMillis = milliSecond + delay;
            nextRuntime = LocalDateTime.ofInstant(Instant.ofEpochMilli(exeInMillis), ZoneId.systemDefault());
        }
    }

    @Override
    public final void run() {
        try {
            // 延迟delay
            SleepUtil.sleepMilliseconds(delay);
            calculateNextRuntime();

            // 任务执行前获取分布式锁， 保证一个任务执行 （注意：各个环境不要争抢同一个锁影响）
            if (!LockUtil.tryLock(getTaskName())) {
                // 可能手动任务抢到了锁，定时任务被挤掉了，只能到一个周期了
                log.info("task [{}] get lock fail", taskName);
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
            progress = 0;

            if (Thread.currentThread().isInterrupted()) {
                // 断中完成，移除等待集合
                // INTERRUPTED
                updateState(State.INTERRUPTED);
                AbstractTask abstractTask = dynamicTaskMap.get(taskName);
                if (abstractTask == this) {
                    dynamicTaskMap.remove(taskName);
                }
            }

            calculateNextRuntime();
            // 释放锁
            // 失败了怎么办？register时会判断!schedule.isDone() 所以不会重复注册
            // 直到下次可重入再次执行任务？下次执行还是同一个线程吗，不是的话LockUtil实现逻辑就不可重入了！！待验证
            LockUtil.unLock(getTaskName());
        }

    }

    /**
     * 怎么限制只能 DynamicTaskService （默认，或者内部类）
     * <p>
     * Java中有四种访问级别：
     * public: 任何外部代码都能访问
     * 默认(无关键字): 只有同一个包中的代码可以访问
     * protected: 只有同一个包中的代码，和这个类的子类代码可以访问
     * private: 任何外部代码都不能访问
     * <p>
     * 因为不想暴漏 dynamicTaskMap 和 manualDynamicTaskMap 任务集合，避免内部逻辑错乱
     */
    void holdTaskMap(Map<String, AbstractTask> dynamicTaskMap) {
        this.dynamicTaskMap = dynamicTaskMap;
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

    /**
     * 上报进度 （protected：只能子类上报进度）
     *
     * @param progress 进度 1 - 100
     */
    protected final void reportProgress(int progress) {
        this.progress = progress;
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

    public final boolean isWaiting() {
        return state == State.WAITING;
    }

    public final boolean isRunning() {
        return state == State.RUNNING;
    }

    public final boolean isInterrupted() {
        return state == State.INTERRUPTED;
    }

    public final int getProgress() {
        return progress;
    }

    public final long getDelay() {
        return delay;
    }

    /**
     * <a href="https://www.concretepage.com/java/java-8/convert-between-java-localdatetime-instant#toInstant">...</a>
     */
    public final LocalDateTime getNextTime() {
        return cronExpression.next(LocalDateTime.now());
    }

    public final LocalDateTime getNextRuntime() {
        return nextRuntime;
    }

    @Override
    public String toString() {
        return "AbstractAsyncTask{" + "taskName='" + taskName + '\'' + ", cron='" + cron + '\'' + ", state=" + state + '}';
    }


    enum State {
        WAITING, RUNNING, FINALLY, INTERRUPTED, TERMINATED
    }


}