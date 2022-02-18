package com.aircraftcarrier.framework.security.core;

import com.aircraftcarrier.framework.security.core.util.SecurityFrameworkUtil;
import org.springframework.lang.Nullable;

import java.util.Objects;
import java.util.Set;

/**
 * 项目中统一使用改用具获取用户信息, 方便后期改动, 扩展
 *
 * @author lzp
 */
public class LoginUserUtil {
    private LoginUserUtil() {
    }

    /**
     * 获取当前用户
     *
     * @return 当前用户
     */
    @Nullable
    public static LoginUser getLoginUser() {
        return SecurityFrameworkUtil.getLoginUser();
    }

    /**
     * 获得当前用户的编号，从上下文中
     *
     * @return 用户编号
     */
    @Nullable
    public static Long getLoginUserId() {
        return SecurityFrameworkUtil.getLoginUserId();
    }

    /**
     * 获得当前用户的角色编号数组
     *
     * @return 角色编号数组
     */
    @Nullable
    public static Set<Long> getLoginUserRoleIds() {
        return SecurityFrameworkUtil.getLoginUserRoleIds();
    }

    /**
     * 获取登录用户名
     *
     * @return
     */
    public static String getLoginUserName() {
        return Objects.requireNonNull(getLoginUser()).getUsername();
    }
}
