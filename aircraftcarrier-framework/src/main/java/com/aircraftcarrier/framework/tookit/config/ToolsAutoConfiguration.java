package com.aircraftcarrier.framework.tookit.config;

import com.aircraftcarrier.framework.tookit.SpringContextUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * @author lzp
 */
@Configuration
public class ToolsAutoConfiguration {

    public ToolsAutoConfiguration(ApplicationContext applicationContext) {
        new SpringContextUtil(applicationContext);
    }

}
