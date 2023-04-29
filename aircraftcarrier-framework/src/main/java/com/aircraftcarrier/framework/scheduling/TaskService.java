package com.aircraftcarrier.framework.scheduling;

import com.aircraftcarrier.framework.concurrent.ExecutorUtil;
import com.aircraftcarrier.framework.concurrent.TraceRunnable;
import com.aircraftcarrier.framework.tookit.SleepUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.concurrent.ScheduledExecutorTask;
import org.springframework.scheduling.support.CronTrigger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;

/**
 * @author liuzhipeng
 * 还可以优化：观察者模式 监听状态， 怎么实现共享状态
 */
@Slf4j
public class TaskService {

    private final Map<String, AbstractTask> dynamicTaskMap = new ConcurrentHashMap<>();
    private final Map<String, AbstractTask> manualDynamicTaskMap = new ConcurrentHashMap<>();
    private final ExecutorService manualService = ExecutorUtil.newCachedThreadPoolDiscard(100, "manual-schedule");
    /**
     * executeOnceManual::for->13, manualService.nThreads = 10, selfCancelService.nThreads = 12。 manualService形成3个触发拒绝，selfCancelService形成1个触发CallerRunsPolicy
     * 此时只要是触发了1个CallerRunsPolicy就形成无限阻塞了
     * 解决1： selfCancelService.nThreads >= for.size ( ThreadPoolUtil.newCachedThreadPool(>=for.size, "self-cancel"); )
     * 解决2： 使用jdk默认的 newCachedThreadPool
     */
    private final ExecutorService selfCancelService = ExecutorUtil.newCachedThreadPoolBlock(10, "self-cancel");
    private final Map<String, ScheduledFuture<?>> scheduledMap = new ConcurrentHashMap<>();
    private final Map<String, Future<?>> manualFutureMap = new ConcurrentHashMap<>();
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
            monitorViewTask.setNextTime(asyncTask.getNextTime());
            monitorViewTask.setDelay(asyncTask.getDelay());
            monitorViewTask.setNextRuntime(asyncTask.getNextRuntime());
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
            schedule = taskScheduler.schedule(new TraceRunnable(task), triggerContext -> {
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

        scheduledFuture.cancel(true);

        // 如果正在等待，需要手动移除
        AbstractTask abstractTask = dynamicTaskMap.get(taskName);
        if (abstractTask.isWaiting()) {
            dynamicTaskMap.remove(taskName);
        }
        // 如果正在运行，任务自己移除
        // dynamicTaskMap.remove(taskName);
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
            Future<?> manualFuture;
            if (null != (manualFuture = manualFutureMap.get(task.getTaskName())) && !manualFuture.isDone()) {
                log.error("manual Future [{}] has been submit ! please cancel before manual", task.getTaskName());
                return false;
            }

            Future<?> f = manualService.submit(task);

            /**
             * {@link DiscardPolicyNew#rejectedExecution(java.lang.Runnable, java.util.concurrent.ThreadPoolExecutor)}
             */
            if (f.isCancelled()) {
                // 大于manualService.最大线程数不执行
                log.info("The task has been triggered with a reject policy!");
                return true;
            }

            manualFutureMap.put(task.getTaskName(), f);
            manualDynamicTaskMap.put(task.getTaskName(), task);
            task.holdTaskMap(manualDynamicTaskMap);

            // 注册异步任务，执行完成自动取消
            selfCancelService.execute(() -> {
                final AbstractTask innerTask = task;
                final Future<?> innerF = f;
                try {
                    while (!innerF.isDone()) {
                        SleepUtil.sleepSeconds(3);
                        log.info("manual task [{}] is done ? ", innerTask.getTaskName());
                        if (innerF.isCancelled()) {
                            break;
                        }
                    }
                } finally {
                    log.info("manual manualFuture [{}] self cancel...", innerTask.getTaskName());
                    innerF.cancel(true);
                    // 等待了很久还没完成，手动取消后又添加执行了，保证移除的是之前的自己，不是新添加的Future
                    removeManualScheduler(manualFutureMap, innerTask.getTaskName(), innerF, (unused) -> log.info("manual manualFuture [{}] self remove", innerTask.getTaskName()));
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
        Future<?> future;
        if (null == (future = manualFutureMap.get(taskName))) {
            log.info("manual future not found!");
            return false;
        }

        future.cancel(true);

        removeManualScheduler(manualFutureMap, taskName, future, (unused) -> log.info("remove manual future: {}", future));
        // scheduledFuture 中的 task.state = RUNNING 为什么不是INTERRUPTED？ cancel是在RUNNING时候异步设置
        return true;
    }

    /**
     * removeManualScheduler
     */
    private void removeManualScheduler(Map<String, Future<?>> manualFutureMap, String taskName, Future<?> f, Consumer<Void> message) {
        synchronized (taskName.intern()) {
            if (f == manualFutureMap.get(taskName)) {
                AbstractTask abstractTask = manualDynamicTaskMap.get(taskName);
                if (abstractTask.isWaiting()) {
                    // 需要手动移除
                    manualDynamicTaskMap.remove(taskName);
                }
                manualFutureMap.remove(taskName);
                message.accept(null);
            }
        }
    }

}