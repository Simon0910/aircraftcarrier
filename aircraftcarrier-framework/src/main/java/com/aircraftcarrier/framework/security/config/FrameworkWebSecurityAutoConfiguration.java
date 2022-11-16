package com.aircraftcarrier.framework.security.config;

import com.aircraftcarrier.framework.security.core.filter.JwtAuthenticationTokenFilter;
import com.aircraftcarrier.framework.security.core.service.SecurityAuthCustomizer;
import com.aircraftcarrier.framework.security.core.service.SecurityAuthFrameworkService;
import com.aircraftcarrier.framework.web.config.WebProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.annotation.Resource;

/**
 * 自定义的 Spring Security 配置适配器实现
 *
 * @author yudao
 */
@ConditionalOnProperty(name = {"framework.security.enabled"}, havingValue = "true")
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Configuration
public class FrameworkWebSecurityAutoConfiguration extends WebSecurityConfigurerAdapter {

    @Resource
    private WebProperties webProperties;

    @Resource
    private SecurityProperties securityProperties;

    /**
     * 自定义用户【认证】逻辑
     */
    @Resource
    private SecurityAuthFrameworkService userDetailsService;

    /**
     * Spring Security 加密器
     */
    @Resource
    private PasswordEncoder passwordEncoder;

    /**
     * 认证失败处理类 Bean
     */
    @Resource
    private AuthenticationEntryPoint authenticationEntryPoint;

    /**
     * 权限不够处理器 Bean
     */
    @Resource
    private AccessDeniedHandler accessDeniedHandler;

    /**
     * 退出处理类 Bean
     */
    @Resource
    private LogoutSuccessHandler logoutSuccessHandler;

    /**
     * 自定义的权限映射 Bean
     *
     * @see #configure(HttpSecurity)
     */
    @Resource
    private SecurityAuthCustomizer<ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry> authorizeRequestsCustomizer;

    /**
     * 由于 Spring Security 创建 AuthenticationManager 对象时，没声明 @Bean 注解，导致无法被注入
     * 通过覆写父类的该方法，添加 @Bean 注解，解决该问题
     */
    @Override
    @Bean
    @ConditionalOnMissingBean(AuthenticationManager.class)
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * 身份认证接口
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    /**
     * 配置 URL 的安全配置
     * <p>
     * anyRequest          |   匹配所有请求路径
     * access              |   SpringEl表达式结果为true时可以访问
     * anonymous           |   匿名可以访问
     * denyAll             |   用户不能访问
     * fullyAuthenticated  |   用户完全认证可以访问（非remember-me下自动登录）
     * hasAnyAuthority     |   如果有参数，参数表示权限，则其中任何一个权限可以访问
     * hasAnyRole          |   如果有参数，参数表示角色，则其中任何一个角色可以访问
     * hasAuthority        |   如果有参数，参数表示权限，则其权限可以访问
     * hasIpAddress        |   如果有参数，参数表示IP地址，如果用户IP和参数匹配，则可以访问
     * hasRole             |   如果有参数，参数表示角色，则其角色可以访问
     * permitAll           |   用户可以任意访问
     * rememberMe          |   允许通过remember-me登录的用户访问
     * authenticated       |   用户登录后可访问
     */
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // 开启跨域
                .cors().and()
                // CSRF 禁用，因为不使用 Session
                .csrf().disable()
                // 将登录框关闭
                .formLogin().disable()
                // 只对当前路径下安全控制, 我很少看到有博客介绍此方法的实际作用
                // https://www.jianshu.com/p/0fbac25654be
                .mvcMatcher("/web/**")
                // 基于 token 机制，所以不需要 Session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().headers().frameOptions().disable().and()
                // 一堆自定义的 Spring Security 处理器
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint).accessDeniedHandler(accessDeniedHandler).and()
                // 登出
                .logout().logoutUrl(api("/logout")).logoutSuccessHandler(logoutSuccessHandler);

        // 设置每个请求的权限 ①：全局共享规则
        httpSecurity.authorizeRequests()
                // 登录的接口，可匿名访问
                .antMatchers(api("/login")).anonymous()
                // 静态资源，可匿名访问
                .antMatchers(HttpMethod.GET, "/*.html", "/**/*.html", "/**/*.css", "/**/*.js", "/favicon.ico").permitAll()
                // 文件的获取接口，可匿名访问
                .antMatchers(api("/infra/file/get/**")).anonymous()
                // Swagger 接口文档
                .antMatchers("/doc.html").anonymous()
                .antMatchers("/v2/api-docs").anonymous()
                .antMatchers("/swagger-ui.html").anonymous()
                .antMatchers("/swagger-resources/**").anonymous()
                .antMatchers("/webjars/**").anonymous()
                .antMatchers("/*/api-docs").anonymous()
                // Spring Boot Actuator 的安全配置
                .antMatchers("/actuator").anonymous().antMatchers("/actuator/**").anonymous()
                // Druid 监控 TODO ：等对接了 druid admin 后，在调整下。
                .antMatchers("/druid/**").anonymous()
                // oAuth2 auth2/login/gitee
                .antMatchers(api("/auth2/login/**")).anonymous().antMatchers(api("/auth2/authorization/**")).anonymous().antMatchers("/api/callback/**").anonymous()
                // 设置每个请求的权限 ②：每个项目的自定义规则
                .and().authorizeRequests(authorizeRequestsCustomizer)
                // 设置每个请求的权限 ③：兜底规则，必须认证
                .authorizeRequests().anyRequest().authenticated();
        // 添加 JWT Filter
        // 此处为什么要new呢？ 因为不想让这个Filter加入到ApplicationFilterChain.filters过滤器链， 指想让此加入DefaultSecurityFilterChain.filters过滤器链,
        // 避免重复执行，引起逻辑上和理解上的混乱
        JwtAuthenticationTokenFilter filter = new JwtAuthenticationTokenFilter(securityProperties, userDetailsService);
        httpSecurity.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
    }

    private String api(String url) {
        return webProperties.getApiPrefix() + url;
    }

}
