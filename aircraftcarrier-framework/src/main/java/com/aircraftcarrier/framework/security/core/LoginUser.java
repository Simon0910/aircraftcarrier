package com.aircraftcarrier.framework.security.core;

import cn.hutool.core.map.MapUtil;
import com.aircraftcarrier.framework.enums.StatusEnum;
import com.aircraftcarrier.framework.security.core.enums.UserTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 登录用户信息
 *
 * @author yudao
 */
@Getter
@Setter
public class LoginUser implements UserDetails {

    /**
     * 用户编号
     */
    private Long id;
    /**
     * 用户类型
     * <p>
     * 关联 {@link UserTypeEnum}
     */
    private Integer userType;
    /**
     * 最后更新时间
     */
    private Date updateTime;

    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 租户编号
     */
    private Long tenantId;

    // ========== UserTypeEnum.ADMIN 独有字段 ==========

    /**
     * 角色编号数组
     * TODO ：可以通过定义一个 Map<String, String> exts 的方式，去除管理员的字段。不过这样会导致系统比较复杂，所以暂时不去掉先；
     */
    private Set<Long> roleIds;

    /**
     * 部门编号
     */
    private Long deptId;

    /**
     * 所属岗位
     */
    private Set<Long> postIds;

    /**
     * group  目前指岗位代替
     * TODO jason：这个字段，改成 postCodes 明确更好哈
     */
    private List<String> groups;

    // ========== 上下文 ==========
    /**
     * 上下文字段，不进行持久化
     * <p>
     * 1. 用于基于 LoginUser 维度的临时缓存
     */
    @JsonIgnore
    private Map<String, Object> context;

    /**
     * 避免序列化
     *
     * @return Password
     */
    @Override
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    /**
     * 避免序列化
     *
     * @return boolean
     */
    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return StatusEnum.ENABLE.getCode().equals(status);
    }

    /**
     * 避免序列化
     *
     * @return
     */
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> list = new ArrayList<>(1);
        // 设置 ROLE_ACTIVITI_USER 角色，保证 activiti7 在 Security 验证时，可以通过。参考 https://juejin.cn/post/6972369247041224712 文章
        // TODO ：这里估计得优化下
        // TODO ：看看有没更优化的方案
        list.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        return list;
    }

    /**
     * 避免序列化
     *
     * @return
     */
    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        // 返回 true，不依赖 Spring Security 判断
        return true;
    }

    /**
     * 避免序列化
     *
     * @return
     */
    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        // 返回 true，不依赖 Spring Security 判断
        return true;
    }

    /**
     * 避免序列化
     *
     * @return
     */
    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        // 返回 true，不依赖 Spring Security 判断
        return true;
    }

    // ========== 上下文 ==========

    public void setContext(String key, Object value) {
        if (context == null) {
            context = new HashMap<>(128);
        }
        context.put(key, value);
    }

    public <T> T getContext(String key, Class<T> type) {
        return MapUtil.get(context, key, type);
    }

}
