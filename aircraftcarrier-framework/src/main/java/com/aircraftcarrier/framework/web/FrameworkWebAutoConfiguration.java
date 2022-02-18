package com.aircraftcarrier.framework.web;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author yudao
 */
@Configuration
@EnableConfigurationProperties({WebProperties.class})
public class FrameworkWebAutoConfiguration implements WebMvcConfigurer {
}
