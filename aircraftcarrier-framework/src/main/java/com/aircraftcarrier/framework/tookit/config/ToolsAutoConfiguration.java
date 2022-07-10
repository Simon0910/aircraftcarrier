package com.aircraftcarrier.framework.tookit.config;

import com.aircraftcarrier.framework.tookit.ApplicationContextUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * @author lzp
 */
@Configuration
public class ToolsAutoConfiguration {

    public ToolsAutoConfiguration(ApplicationContext applicationContext) {
        ApplicationContextUtil.setApplicationContext(applicationContext);
    }

}
