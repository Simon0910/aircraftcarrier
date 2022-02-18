package com.aircraftcarrier.framework.support.config;

import com.aircraftcarrier.framework.core.constant.WebFilterOrderConstant;
import com.aircraftcarrier.framework.support.filter.TraceIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * @author lzp
 */
@Configuration
public class FilterAutoConfiguration {

    /**
     * TraceIdFilter
     *
     * @return FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean<TraceIdFilter> registerTraceIdFilter() {
        FilterRegistrationBean<TraceIdFilter> bean = new FilterRegistrationBean<>();
        bean.setUrlPatterns(Arrays.asList("/web/*", "/mobile/*", "/login", "/logout"));
        bean.setFilter(new TraceIdFilter());
        // 这个顺序很重要哦，为避免麻烦请设置在最前
        bean.setOrder(WebFilterOrderConstant.TRACE_FILTER);
        return bean;
    }
}
