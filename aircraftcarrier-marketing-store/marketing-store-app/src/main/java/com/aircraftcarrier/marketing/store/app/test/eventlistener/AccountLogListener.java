package com.aircraftcarrier.marketing.store.app.test.eventlistener;

import cn.hutool.json.JSONUtil;
import com.aircraftcarrier.framework.security.core.LoginUser;
import com.aircraftcarrier.framework.security.core.LoginUserUtil;
import com.aircraftcarrier.framework.tookit.JsonUtils;
import com.aircraftcarrier.marketing.store.domain.event.AccountEvent;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author lzp
 */
@Log4j2
@Component
public class AccountLogListener {

    @Async
    @EventListener(AccountEvent.class)
    public void onApplicationEvent(AccountEvent accountEvent) {
        log.info("Async AccountLogListener 触发器触发了,参数：{}", JSONUtil.toJsonStr(accountEvent.getData()));
        LoginUser loginUser3 = LoginUserUtil.getLoginUser();
        log.info("3-LoginUser：{}", JsonUtils.obj2Json(loginUser3));
    }

    @EventListener(AccountEvent.class)
    public void onApplicationEventSync(AccountEvent accountEvent) {
        log.info("Sync AccountLogListener 触发器触发了,参数：{}", JSONUtil.toJsonStr(accountEvent.getData()));
        LoginUser loginUser4 = LoginUserUtil.getLoginUser();
        log.info("4-LoginUser：{}", JsonUtils.obj2Json(loginUser4));
    }
}