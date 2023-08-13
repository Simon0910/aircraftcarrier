package com.aircraftcarrier.framework.exceltask;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * ExcelTaskAutoConfiguration
 *
 * @author zhipengliu
 * @date 2023/4/1
 * @since 1.0
 */
@AutoConfiguration
public class ExcelTaskAutoConfiguration {

    @Bean
    public TaskExecutor workTask() {
        return new TaskExecutor();
    }
}
