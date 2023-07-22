package com.aircraftcarrier.framework.support.config;

import com.aircraftcarrier.framework.support.ApplicationEventAllListener;
import com.aircraftcarrier.framework.support.ApplicationEventListener;
import com.aircraftcarrier.framework.support.ApplicationPrintInfoRunner;
import com.aircraftcarrier.framework.support.context.EnumMappingContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lzp
 */
@Configuration
public class SupportAutoConfiguration {

    @Value("${scan.base.packages:com.aircraftcarrier}")
    private String scanBasePackages;

    @Bean
    public EnumMappingContext enumMappingContext() {
        return new EnumMappingContext(scanBasePackages);
    }

    @Bean
    public ApplicationPrintInfoRunner applicationPrintInfoRunner() {
        return new ApplicationPrintInfoRunner();
    }

    @Bean
    public ApplicationEventAllListener applicationEventListener() {
        return new ApplicationEventAllListener();
    }

    @Bean
    public ApplicationEventListener availabilityListener() {
        return new ApplicationEventListener();
    }
}
