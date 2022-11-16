package com.aircraftcarrier.security.adapter.config;

import com.aircraftcarrier.framework.security.core.service.SecurityAuthCustomizer;
import com.aircraftcarrier.framework.web.config.WebProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;

import javax.annotation.Resource;

/**
 * @author yudao
 */
@Configuration
public class SecurityAuthCustomizerConfig implements SecurityAuthCustomizer<ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry> {

    @Resource
    private WebProperties webProperties;

    @Value("${spring.boot.admin.context-path:''}")
    private String adminSeverContextPath;

    @Override
    public void customize(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry expressionInterceptUrlRegistry) {
        // 验证码的接口
        expressionInterceptUrlRegistry.antMatchers(api("/system/captcha/**")).anonymous();
        // 获得租户编号的接口
        expressionInterceptUrlRegistry.antMatchers(api("/system/tenant/get-id-by-name")).anonymous();
        // Spring Boot Admin Server 的安全配置
        expressionInterceptUrlRegistry.antMatchers(adminSeverContextPath).anonymous()
                .antMatchers(adminSeverContextPath + "/**").anonymous();
        // 短信回调 API
        expressionInterceptUrlRegistry.antMatchers(api("/system/sms/callback/**")).anonymous();
        expressionInterceptUrlRegistry.antMatchers(api("/**")).authenticated();
    }

    private String api(String url) {
        return webProperties.getApiPrefix() + url;
    }
}
