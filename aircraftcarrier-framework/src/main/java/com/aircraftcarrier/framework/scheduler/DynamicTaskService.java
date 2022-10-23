package com.aircraftcarrier.framework.scheduler;

import com.aircraftcarrier.framework.tookit.ThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

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
    public Map<String, ScheduledFuture<?>> scheduledMap = new ConcurrentHashMap<>();
    public Map<String, FutureTask<?>> manualScheduledMap = new ConcurrentHashMap<>();
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
                log.error("schedule task [{}] has been register ! please cancel before register", task.getTaskName());
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

            log.info("add :: {}", schedule);
            return true;
        }
    }

    /**
     * 手动执行一次
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
            FutureTask<?> manualSchedule;
            if (null != (manualSchedule = manualScheduledMap.get(task.getTaskName())) && !manualSchedule.isDone()) {
                log.error("manual schedule task [{}] has been register ! please cancel before register", task.getTaskName());
                return false;
            }

            FutureTask<Void> f = new FutureTask<>(task, null);
            manualService.execute(f);

            manualScheduledMap.put(task.getTaskName(), f);
            manualDynamicTaskMap.put(task.getTaskName(), task);
            task.holdTaskMap(manualDynamicTaskMap);

            // 注册异步任务，执行完成自动取消
            selfCancelService.execute(() -> {
                final AbstractAsyncTask innerTask = task;
                try {
                    f.get(2, TimeUnit.HOURS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    log.error("manual schedule ERROR: ", e);
                } finally {
                    log.info("manual schedule task [{}] self cancel...", innerTask.getTaskName());
                    f.cancel(true);
                    // 等待了很久还没完成，手动取消后又添加执行了，保证移除的是之前的自己，不是新添加的Future
                    removeManualScheduler(manualScheduledMap, innerTask.getTaskName(), f, (unused) -> log.info("manual schedule task [{}] self remove", innerTask.getTaskName()));
                }
            });
            log.info("executeOnce :: {}", f);
            return true;
        }
    }

    private void removeManualScheduler(Map<String, FutureTask<?>> scheduledMap, String taskName, Future<?> f, Consumer<Void> message) {
        synchronized (taskName.intern()) {
            if (f == scheduledMap.get(taskName)) {
                message.accept(null);
                scheduledMap.remove(taskName);
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
        FutureTask<?> future;
        if (null == (future = manualScheduledMap.get(taskName))) {
            log.info("manual schedule not found!");
            return false;
        }

        boolean cancel = future.cancel(true);
        log.info("manual schedule cancel... {}", cancel);
        log.info("manual schedule is done: {}", future.isDone());

        removeManualScheduler(manualScheduledMap, taskName, future, (unused) -> log.info("remove manual schedule task: {}", future));
        // scheduledFuture 中的 task.state = RUNNING 为什么不是INTERRUPTED？ cancel是在RUNNING时候异步设置
        return true;
    }
}