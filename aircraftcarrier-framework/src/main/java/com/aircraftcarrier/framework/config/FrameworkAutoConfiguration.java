package com.aircraftcarrier.framework.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * FrameworkAutoConfiguration
 *
 * @author zhipengliu
 * @date 2022/9/1
 * @since 1.0
 */
@AutoConfiguration
@EnableConfigurationProperties({FrameworkProperties.class})
public class FrameworkAutoConfiguration {

}
