package com.aircraftcarrier.framework.scheduling;

import com.aircraftcarrier.framework.tookit.ThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.concurrent.ScheduledExecutorTask;
import org.springframework.scheduling.support.CronTrigger;

import java.util.ArrayList;
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
public class TaskService {

    private final Map<String, AbstractTask> dynamicTaskMap = new ConcurrentHashMap<>();
    private final Map<String, AbstractTask> manualDynamicTaskMap = new ConcurrentHashMap<>();
    private final ExecutorService manualService = ThreadPoolUtil.newCachedThreadPoolDiscard(100, "manual-schedule");
    /**
     * executeOnceManual::for->13, manualService.nThreads = 10, selfCancelService.nThreads = 12。 manualService形成3个触发拒绝，selfCancelService形成1个触发CallerRunsPolicy
     * 此时只要是触发了1个CallerRunsPolicy就形成无限阻塞了
     * 解决1： selfCancelService.nThreads >= for.size ( ThreadPoolUtil.newCachedThreadPool(>=for.size, "self-cancel"); )
     * 解决2： 使用jdk默认的 newCachedThreadPool
     */
    private final ExecutorService selfCancelService = ThreadPoolUtil.newCachedThreadPool("self-cancel");
    private final Map<String, ScheduledFuture<?>> scheduledMap = new ConcurrentHashMap<>();
    private final Map<String, FutureTask<?>> manualFutureTaskMap = new ConcurrentHashMap<>();
    private final ConcurrentTaskScheduler taskScheduler;

    /**
     * 同一时刻最多有几个任务在运行 corePoolSize
     *
     * @see TaskSchedulingAutoConfiguration#concurrentTaskScheduler()
     */
    public TaskService(ConcurrentTaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    private static List<TaskMonitorView> getMonitorViewTasks(List<AbstractTask> abstractAsyncTasks) {
        List<TaskMonitorView> results = new ArrayList<>(abstractAsyncTasks.size());
        for (AbstractTask asyncTask : abstractAsyncTasks) {
            TaskMonitorView monitorViewTask = new TaskMonitorView();
            monitorViewTask.setTaskName(asyncTask.getTaskName());
            monitorViewTask.setCron(asyncTask.getCron());
            monitorViewTask.setState(asyncTask.getState().toString());
            monitorViewTask.setProgress(asyncTask.getProgress());
            results.add(monitorViewTask);
        }
        return results;
    }

    /**
     * 查看已开启但还未执行的动态任务
     */
    public List<TaskMonitorView> getWaitingTaskList() {
        List<AbstractTask> abstractAsyncTasks = dynamicTaskMap.values().stream().filter(AbstractTask::isWaiting).toList();
        return getMonitorViewTasks(abstractAsyncTasks);
    }

    public List<TaskMonitorView> getRunningTaskList() {
        List<AbstractTask> abstractAsyncTasks = dynamicTaskMap.values().stream().filter(AbstractTask::isRunning).toList();
        return getMonitorViewTasks(abstractAsyncTasks);
    }

    public List<TaskMonitorView> getTaskList() {
        List<AbstractTask> abstractAsyncTasks = dynamicTaskMap.values().stream().toList();
        return getMonitorViewTasks(abstractAsyncTasks);
    }

    /**
     * 添加一个动态任务
     *
     * @param task task
     * @return boolean
     */
    public boolean register(AbstractTask task) {
        // synchronized 避免重复注册同一个任务
        synchronized (task.getTaskName().intern()) {
            // 如果任务正在running
            AbstractTask manualAsyncTask = manualDynamicTaskMap.get(task.getTaskName());
            if (manualAsyncTask != null && manualAsyncTask.isRunning()) {
                // 手动任务正在执行
                log.error("manual task [{}] is already running...", task.getTaskName());
                return false;
            }
            AbstractTask asyncTask = dynamicTaskMap.get(task.getTaskName());
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

            // taskScheduler
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
     * 停止任务
     *
     * @param task task
     * @return boolean
     */
    public boolean cancel(AbstractTask task) {
        String taskName = task.getTaskName();
        Future<?> scheduledFuture;
        if (null == (scheduledFuture = scheduledMap.get(taskName))) {
            log.info("schedule not found!");
            return false;
        }

        boolean cancel = scheduledFuture.cancel(true);
        log.info("schedule canceled... {}", cancel);
        log.info("schedule is done: {}", scheduledFuture.isDone());

        dynamicTaskMap.remove(taskName);
        scheduledMap.remove(taskName);
        // scheduledFuture 中的 task.state = RUNNING 为什么不是INTERRUPTED？ cancel是在RUNNING时候异步设置
        log.info("remove schedule task: {}", scheduledFuture);
        return true;
    }

    /**
     * 手动执行一次
     * {@link ScheduledExecutorTask#isOneTimeTask()}
     * <p>
     * 注意： 定时未开始前允许手动执行
     * 注意：大于manualService.最大线程数不执行
     *
     * @param task task
     */
    public boolean executeOnceManual(AbstractTask task) {
        // synchronized 避免重复注册同一个任务
        synchronized (task.getTaskName().intern()) {
            // 如果任务正在running
            AbstractTask asyncTask = dynamicTaskMap.get(task.getTaskName());
            if (asyncTask != null && asyncTask.isRunning()) {
                // 定时任务正在运行
                log.error("schedule task [{}] is already running...", task.getTaskName());
                return false;
            }
            AbstractTask manualAsyncTask = manualDynamicTaskMap.get(task.getTaskName());
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
                final AbstractTask innerTask = task;
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

    /**
     * 停止任务
     *
     * @param task task
     * @return boolean
     */
    public boolean cancelManual(AbstractTask task) {
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
     * removeManualScheduler
     */
    private void removeManualScheduler(Map<String, FutureTask<?>> manualFutureTaskMap, String taskName, Future<?> f, Consumer<Void> message) {
        synchronized (taskName.intern()) {
            if (f == manualFutureTaskMap.get(taskName)) {
                message.accept(null);
                manualDynamicTaskMap.remove(taskName);
                manualFutureTaskMap.remove(taskName);
            }
        }
    }

}