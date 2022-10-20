package com.aircraftcarrier.framework.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

/**
 * @author liuzhipeng
 */
@Slf4j
public class DynamicTaskService {

    private final ThreadPoolTaskScheduler taskScheduler;
    /**
     * 以下两个都是线程安全的集合类。
     */
    public Map<String, ScheduledFuture<?>> taskMap = new ConcurrentHashMap<>();
    public List<String> taskList = new CopyOnWriteArrayList<>();

    public DynamicTaskService(ThreadPoolTaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    /**
     * 查看已开启但还未执行的动态任务
     *
     * @return
     */
    public List<String> getTaskList() {
        return taskList;
    }

    /**
     * 添加一个动态任务
     *
     * @param task task
     * @return boolean
     */
    public boolean add(AbstractAsyncTask task) {
        // 此处的逻辑是 ，如果当前已经有这个名字的任务存在，先删除之前的，再添加现在的。（即重复就覆盖）
        if (null != taskMap.get(task.getTaskName())) {
            stop(task);
        }

        // schedule :调度给定的Runnable ，在指定的执行时间调用它。
        //一旦调度程序关闭或返回的ScheduledFuture被取消，执行将结束。
        //参数：
        //任务 – 触发器触发时执行的 Runnable
        //startTime – 任务所需的执行时间（如果这是过去，则任务将立即执行，即尽快执行）
        ScheduledFuture<?> schedule = taskScheduler.schedule(task, triggerContext -> {
            // 使用CronTrigger触发器，可动态修改cron表达式来操作循环规则
            Trigger trigger = new CronTrigger(task.getCron());
            // 使用不同的触发器，为设置循环时间的关键，区别于CronTrigger触发器，该触发器可随意设置循环间隔时间，单位为毫秒
//            Trigger trigger = new PeriodicTrigger(timer);
            return trigger.nextExecutionTime(triggerContext);
        });
        taskMap.put(task.getTaskName(), schedule);
        taskList.add(task.getTaskName());
        log.info("add {}", schedule);
        return true;
    }


    /**
     * 停止任务
     *
     * @param task task
     * @return boolean
     */
    public boolean stop(AbstractAsyncTask task) {
        String taskName = task.getTaskName();
        ScheduledFuture<?> scheduledFuture;
        if (null == (scheduledFuture = taskMap.get(taskName))) {
            return false;
        }
        boolean canceled = scheduledFuture.cancel(true);
        log.info("task canceled: {}", canceled);
        log.info("task is done: {}", scheduledFuture.isDone());
        taskMap.remove(taskName);
        taskList.remove(taskName);
        log.info("remove task: {}", scheduledFuture);
        return true;
    }
}