package com.aircraftcarrier.framework.security.core.filter;

import cn.hutool.core.util.StrUtil;
import com.aircraftcarrier.framework.exception.ErrorCode;
import com.aircraftcarrier.framework.security.config.SecurityProperties;
import com.aircraftcarrier.framework.security.core.LoginUser;
import com.aircraftcarrier.framework.security.core.service.SecurityAuthFrameworkService;
import com.aircraftcarrier.framework.security.core.util.SecurityFrameworkUtil;
import com.aircraftcarrier.framework.tookit.ResponseWriterUtil;
import lombok.AllArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT 过滤器，验证 token 的有效性
 * 验证通过后，获得 {@link LoginUser} 信息，并加入到 Spring Security 上下文
 *
 * @author yudao
 */
@AllArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;

    private final SecurityAuthFrameworkService authService;

    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = SecurityFrameworkUtil.obtainAuthorization(request, securityProperties.getTokenHeader());
        if (StrUtil.isBlank(token)) {
            ResponseWriterUtil.handlerExceptionMessage(ErrorCode.UNAUTHORIZED, "token must not be null", response);
            return;
        }

        try {
            // 验证 token 有效性
            LoginUser loginUser = authService.verifyTokenAndRefresh(token);
            // 模拟 Login 功能，方便日常开发调试
            if (loginUser == null) {
                loginUser = this.mockLoginUser(token);
            }
            // 设置当前用户
            if (loginUser != null) {
                SecurityFrameworkUtil.setLoginUser(loginUser, request);
            }
        } catch (Throwable ex) {
            logger.error("验证 token 失败", ex);
            ResponseWriterUtil.handlerExceptionMessage(ErrorCode.TOKEN_INVALID, "验证 token 失败", response);
            return;
        }

        // 继续过滤链
        chain.doFilter(request, response);
    }

    /**
     * 模拟登录用户，方便日常开发调试
     * <p>
     * 注意，在线上环境下，一定要关闭该功能！！！
     *
     * @param token 模拟的 token，格式为 {@link SecurityProperties#getTokenSecret()} + 用户编号
     * @return 模拟的 LoginUser
     */
    private LoginUser mockLoginUser(String token) {
        if (!securityProperties.getMock()) {
            return null;
        }
        // 必须以 mockSecret 开头
        if (!token.startsWith(securityProperties.getMockSecret())) {
            return null;
        }
        Long userId = Long.valueOf(token.substring(securityProperties.getMockSecret().length()));
        return authService.mockLogin(userId);
    }

}
