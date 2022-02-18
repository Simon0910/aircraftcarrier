package com.aircraftcarrier.framework.web;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;

/**
 * @author yudao
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "framework.web")
public class WebProperties {

    /**
     * API 前缀，实现所有 Controller 提供的 RESTFul API 的统一前缀
     * <p>
     * <p>
     * 意义：通过该前缀，避免 Swagger、Actuator 意外通过 Nginx 暴露出来给外部，带来安全性问题
     * 这样，Nginx 只需要配置转发到 /api/* 的所有接口即可。
     *
     * @see FrameworkWebAutoConfiguration#configurePathMatch(PathMatchConfigurer)
     */
    private String apiPrefix = "api";

    /**
     * Controller 所在包
     * <p>
     * 主要目的是，给该 Controller 设置指定的 {@link #apiPrefix}
     * <p>
     * 因为我们有多个 modules 包里会包含 Controller，所以只需要写到 cn.iocoder.yudao 这样的层级
     */
    private String controllerPackage;

}
