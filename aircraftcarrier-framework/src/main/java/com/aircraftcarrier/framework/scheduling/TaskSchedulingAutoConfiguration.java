package com.aircraftcarrier.framework.scheduling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * @author liuzhipeng
 */
@Slf4j
@AutoConfiguration(after = org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration.class)
public class TaskSchedulingAutoConfiguration {

    @Bean
    public DynamicTaskService dynamicTaskService(ThreadPoolTaskScheduler taskScheduler) {
        return new DynamicTaskService(taskScheduler);
    }
}
