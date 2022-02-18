package com.aircraftcarrier.framework.security.core.service;

import com.aircraftcarrier.framework.exception.BizException;
import com.aircraftcarrier.framework.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

/**
 * @author lzp
 */
@Slf4j
public abstract class AbstractUsernamePasswordAuthentication implements SecurityAuthFrameworkService {
    @Value("${framework.security.enabled:false}")
    private boolean securityEnable;

    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;


    protected UserDetails authentication(String username, String password) {
        if (!securityEnable) {
            return loadUserByUsername(username);
        }

        // 用户验证
        Authentication authentication;
        try {
            // 调用 Spring Security 的 AuthenticationManager#authenticate(...) 方法，使用账号密码进行认证
            // 在其内部，会调用到 loadUserByUsername 方法，获取 User 信息
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (BadCredentialsException badCredentialsException) {
            log.info("账号或密码不正确");
            throw new BizException(ErrorCode.ACCOUNT_PASSWORD_ERROR, "账号或密码错误");
        } catch (DisabledException disabledException) {
            log.info("用户被禁用");
            throw new BizException(ErrorCode.ACCOUNT_DISABLE, "用户被禁用");
        } catch (AuthenticationException authenticationException) {
            log.error("[login0][username({}) 发生未知异常]", username, authenticationException);
            throw new BizException(ErrorCode.LONG_FAIL, "登录失败");
        }
        // 登录成功的日志
        Assert.notNull(authentication.getPrincipal(), "Principal 不会为空");
        return (UserDetails) authentication.getPrincipal();
    }
}
