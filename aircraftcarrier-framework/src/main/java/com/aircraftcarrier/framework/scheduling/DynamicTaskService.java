package com.aircraftcarrier.framework.scheduling;

import com.aircraftcarrier.framework.cache.LockUtil;
import com.aircraftcarrier.framework.tookit.ThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ScheduledExecutorTask;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * @author liuzhipeng
 * 还可以优化：观察者模式 监听状态， 怎么实现共享状态
 */
@Slf4j
public class DynamicTaskService {

    private final Map<String, AbstractAsyncTask> dynamicTaskMap = new ConcurrentHashMap<>();
    private final Map<String, AbstractAsyncTask> manualDynamicTaskMap = new ConcurrentHashMap<>();
    private final ExecutorService manualService = ThreadPoolUtil.newCachedThreadPoolDiscard(10, "manual-schedule");
    private final ExecutorService selfCancelService = ThreadPoolUtil.newCachedThreadPool(10, "self-cancel");
    private final Map<String, ScheduledFuture<?>> scheduledMap = new ConcurrentHashMap<>();
    private final Map<String, FutureTask<?>> manualFutureTaskMap = new ConcurrentHashMap<>();
    private final ThreadPoolTaskScheduler taskScheduler;

    public DynamicTaskService(ThreadPoolTaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    /**
     * 查看已开启但还未执行的动态任务
     */
    public List<String> getWaitingTaskList() {
        return dynamicTaskMap.entrySet().stream().filter(e -> e.getValue().istWaiting()).toList().stream().map(Map.Entry::getKey).toList();
    }

    public List<String> getRunningTaskList() {
        return dynamicTaskMap.entrySet().stream().filter(e -> e.getValue().isRunning()).toList().stream().map(Map.Entry::getKey).toList();
    }

    /**
     * 添加一个动态任务
     *
     * @param task task
     * @return boolean
     */
    public boolean register(AbstractAsyncTask task) {
        // synchronized 避免重复注册同一个任务
        synchronized (task.getTaskName().intern()) {
            // 如果任务正在running
            AbstractAsyncTask manualAsyncTask = manualDynamicTaskMap.get(task.getTaskName());
            if (manualAsyncTask != null && manualAsyncTask.isRunning()) {
                // 手动任务正在执行
                log.error("manual task [{}] is already running...", task.getTaskName());
                return false;
            }
            AbstractAsyncTask asyncTask = dynamicTaskMap.get(task.getTaskName());
            if (asyncTask != null && asyncTask.isRunning()) {
                // 定时任务正在运行
                log.error("schedule task [{}] is already running...", task.getTaskName());
                return false;
            }

            // 上一个定时任务还没取消
            ScheduledFuture<?> schedule;
            if (null != (schedule = scheduledMap.get(task.getTaskName())) && !schedule.isDone()) {
                log.error("ScheduledFuture [{}] has been register ! please cancel before register", task.getTaskName());
                return false;
            }

            // schedule :调度给定的Runnable ，在指定的执行时间调用它。
            //一旦调度程序关闭或返回的ScheduledFuture被取消，执行将结束。
            //参数：
            //任务 – 触发器触发时执行的 Runnable
            //startTime – 任务所需的执行时间（如果这是过去，则任务将立即执行，即尽快执行）
            schedule = taskScheduler.schedule(task, triggerContext -> {
                // 使用CronTrigger触发器，可动态修改cron表达式来操作循环规则
                Trigger trigger = new CronTrigger(task.getCron());
                // 使用不同的触发器，为设置循环时间的关键，区别于CronTrigger触发器，该触发器可随意设置循环间隔时间，单位为毫秒
//            Trigger trigger = new PeriodicTrigger(timer);
                return trigger.nextExecutionTime(triggerContext);
            });
            scheduledMap.put(task.getTaskName(), schedule);
            // 相互引用会有问题吗？怎么验证？
            dynamicTaskMap.put(task.getTaskName(), task);
            task.holdTaskMap(dynamicTaskMap);

            log.info("register :: {}", schedule);
            return true;
        }
    }

    /**
     * 手动执行一次
     * {@link ScheduledExecutorTask#isOneTimeTask()}
     * <p>
     * 注意： 定时未开始前允许手动执行
     *
     * @param task task
     */
    public boolean executeOnceManual(AbstractAsyncTask task) {
        // synchronized 避免重复注册同一个任务
        synchronized (task.getTaskName().intern()) {
            // 如果任务正在running
            AbstractAsyncTask asyncTask = dynamicTaskMap.get(task.getTaskName());
            if (asyncTask != null && asyncTask.isRunning()) {
                // 定时任务正在运行
                log.error("schedule task [{}] is already running...", task.getTaskName());
                return false;
            }
            AbstractAsyncTask manualAsyncTask = manualDynamicTaskMap.get(task.getTaskName());
            if (manualAsyncTask != null && manualAsyncTask.isRunning()) {
                // 手动任务正在执行
                log.error("manual task [{}] is already running...", task.getTaskName());
                return false;
            }

            // 上一个手动任务还没取消
            FutureTask<?> manualFutureTask;
            if (null != (manualFutureTask = manualFutureTaskMap.get(task.getTaskName())) && !manualFutureTask.isDone()) {
                log.error("manual FutureTask [{}] has been register ! please cancel before register", task.getTaskName());
                return false;
            }

            FutureTask<Void> f = new FutureTask<>(task, null);
            manualService.execute(f);

            manualFutureTaskMap.put(task.getTaskName(), f);
            manualDynamicTaskMap.put(task.getTaskName(), task);
            task.holdTaskMap(manualDynamicTaskMap);

            // 注册异步任务，执行完成自动取消
            selfCancelService.execute(() -> {
                final AbstractAsyncTask innerTask = task;
                try {
                    f.get(2, TimeUnit.HOURS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    log.error("manual manualFutureTask ERROR: ", e);
                } finally {
                    log.info("manual manualFutureTask [{}] self cancel...", innerTask.getTaskName());
                    f.cancel(true);
                    // 等待了很久还没完成，手动取消后又添加执行了，保证移除的是之前的自己，不是新添加的Future
                    removeManualScheduler(manualFutureTaskMap, innerTask.getTaskName(), f, (unused) -> log.info("manual manualFutureTask [{}] self remove", innerTask.getTaskName()));
                }
            });
            log.info("executeOnce :: {}", f);
            return true;
        }
    }


    private void removeManualScheduler(Map<String, FutureTask<?>> manualFutureTaskMap, String taskName, Future<?> f, Consumer<Void> message) {
        synchronized (taskName.intern()) {
            if (f == manualFutureTaskMap.get(taskName)) {
                message.accept(null);
                manualFutureTaskMap.remove(taskName);
            }
        }
    }


    /**
     * 停止任务
     *
     * @param task task
     * @return boolean
     */
    public boolean cancel(AbstractAsyncTask task) {
        String taskName = task.getTaskName();
        Future<?> scheduledFuture;
        if (null == (scheduledFuture = scheduledMap.get(taskName))) {
            log.info("schedule not found!");
            return false;
        }

        boolean cancel = scheduledFuture.cancel(true);
        log.info("schedule canceled... {}", cancel);
        log.info("schedule is done: {}", scheduledFuture.isDone());

        scheduledMap.remove(taskName);
        // scheduledFuture 中的 task.state = RUNNING 为什么不是INTERRUPTED？ cancel是在RUNNING时候异步设置
        log.info("remove schedule task: {}", scheduledFuture);
        return true;
    }


    /**
     * 停止任务
     *
     * @param task task
     * @return boolean
     */
    public boolean cancelManual(AbstractAsyncTask task) {
        String taskName = task.getTaskName();
        FutureTask<?> futureTask;
        if (null == (futureTask = manualFutureTaskMap.get(taskName))) {
            log.info("manual futureTask not found!");
            return false;
        }

        boolean cancel = futureTask.cancel(true);
        log.info("manual futureTask cancel... {}", cancel);
        log.info("manual futureTask is done: {}", futureTask.isDone());

        removeManualScheduler(manualFutureTaskMap, taskName, futureTask, (unused) -> log.info("remove manual futureTask: {}", futureTask));
        // scheduledFuture 中的 task.state = RUNNING 为什么不是INTERRUPTED？ cancel是在RUNNING时候异步设置
        return true;
    }

    /**
     * @author liuzhipeng
     * <p>
     * 为什么要使用内部类？ 保证内部逻辑安全性 因为不想暴漏 dynamicTaskMap 和 manualDynamicTaskMap 任务集合，避免内部逻辑错乱（暂时没想到更好的方案）
     */
    @Slf4j
    public abstract static class AbstractAsyncTask implements Runnable {

        private final String taskName;
        private final String cron;
        /**
         * state
         * volatile 为了定时线程和手动线程相互及时的看到
         */
        private volatile State state;
        private Map<String, AbstractAsyncTask> dynamicTaskMap;

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

        /**
         * 怎么限制只能 DynamicTaskService
         */
        private void holdTaskMap(Map<String, AbstractAsyncTask> dynamicTaskMap) {
            this.dynamicTaskMap = dynamicTaskMap;
        }

        @Override
        public String toString() {
            return "AbstractAsyncTask{" + "taskName='" + taskName + '\'' + ", cron='" + cron + '\'' + ", state=" + state + '}';
        }

        enum State {
            WAITING, RUNNING, FINALLY, INTERRUPTED, TERMINATED
        }
    }
}