package com.aircraftcarrier.marketing.store.app;

import com.aircraftcarrier.framework.enums.StatusEnum;
import com.aircraftcarrier.framework.security.core.LoginUser;
import com.aircraftcarrier.framework.security.core.service.AbstractUsernamePasswordAuthentication;
import com.aircraftcarrier.marketing.store.client.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author lzp
 */
@Slf4j
@Service
public class AuthServiceImpl extends AbstractUsernamePasswordAuthentication implements AuthService {

    @Override
    public LoginUser verifyTokenAndRefresh(String token) {
        // 建议拦截器把LoginUserInfo放进缓存 (token: LoginUserInfo)
        return null;
    }

    @Override
    public LoginUser mockLogin(Long userId) {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(1L);
        loginUser.setUsername("admin");
        loginUser.setStatus(StatusEnum.ENABLE.code());
        return loginUser;
    }

    @Override
    public void logout(String token) {

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }


}
