package com.aircraftcarrier.framework.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * @author lzp
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "framework.security")
public class SecurityProperties {

    /**
     * HTTP 请求时，访问令牌的请求 Header
     */
    private String tokenHeader = "Authorization";

    /**
     * Token 过期时间
     */
    private Duration tokenTimeout = Duration.ofMinutes(30);

    /**
     * Token 秘钥
     */
    private String tokenSecret = "abcdefghijklmnopqrstuvwxyz";

    /**
     * Session 过期时间
     * <p>
     * 当 User 用户超过当前时间未操作，则 Session 会过期
     */
    private Duration sessionTimeout = Duration.ofMinutes(30);

    /**
     * mock 模式的开关
     */
    private Boolean mock = false;

    /**
     * mock 模式的秘钥
     * 一定要配置秘钥，保证安全性
     * 这里设置了一个默认值，因为实际上只有 mockEnable 为 true 时才需要配置。
     */
    private String mockSecret = "mock-";

}
