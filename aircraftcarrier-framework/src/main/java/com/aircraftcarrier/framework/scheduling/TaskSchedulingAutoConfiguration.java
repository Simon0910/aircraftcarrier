package com.aircraftcarrier.framework.scheduling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import java.util.concurrent.Executors;

/**
 * @author liuzhipeng
 */
@Slf4j
@AutoConfiguration(after = org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration.class)
public class TaskSchedulingAutoConfiguration {

    @Bean
    public DynamicTaskService dynamicTaskService(ConcurrentTaskScheduler taskScheduler) {
        return new DynamicTaskService(taskScheduler);
    }

    @Bean
    public ConcurrentTaskScheduler concurrentTaskScheduler() {
        ConcurrentTaskScheduler concurrentTaskScheduler = new ConcurrentTaskScheduler();
        // 同一时刻最多有几个任务在运行 corePoolSize
        concurrentTaskScheduler.setScheduledExecutor(Executors.newScheduledThreadPool(10));
        return concurrentTaskScheduler;
    }
}
