package com.aircraftcarrier.framework.security.disabled;

import com.aircraftcarrier.framework.core.constant.WebFilterOrderConstant;
import com.aircraftcarrier.framework.security.config.SecurityProperties;
import com.aircraftcarrier.framework.security.core.filter.JwtAuthenticationTokenFilter;
import com.aircraftcarrier.framework.security.core.service.SecurityAuthFrameworkService;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.util.Collections;

/**
 * @author lzp
 * ConditionalOnExpression("!'${framework.security.enabled}'.equals('true')")
 */
@AllArgsConstructor
@ConditionalOnProperty(name = {"framework.security.enabled"}, havingValue = "false")
@EnableConfigurationProperties(SecurityProperties.class)
public class FrameworkSecurityDisableAutoConfiguration {

    private final SecurityProperties securityProperties;

    private final SecurityAuthFrameworkService authService;

    /**
     * GeneralTokenFilter
     *
     * @return FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationTokenFilter> registerGeneralTokenFilter() {
        FilterRegistrationBean<JwtAuthenticationTokenFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new JwtAuthenticationTokenFilter(securityProperties, authService));
        bean.setUrlPatterns(Collections.singletonList("/web/*"));
        // 这个顺序很重要哦
        bean.setOrder(WebFilterOrderConstant.GENERAL_TOKEN_FILTER);
        return bean;
    }

}
