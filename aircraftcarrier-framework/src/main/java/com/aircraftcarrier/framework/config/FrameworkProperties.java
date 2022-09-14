package com.aircraftcarrier.framework.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * FrameworkProperties
 *
 * @author zhipengliu
 * @date 2022/8/12
 * @since 1.0
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "framework")
public class FrameworkProperties {
    private int okHttpClientConnectTimeout = 10;
    private int okHttpClientReadTimeout = 10;
    private int okHttpClientWriteTimeout = 10;
    private String authentication = "xxx";
}
