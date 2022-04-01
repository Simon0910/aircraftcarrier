package com.aircraftcarrier.marketing.store.app;

import com.aircraftcarrier.framework.cache.LockUtil;
import com.aircraftcarrier.framework.tookit.ObjUtil;
import com.aircraftcarrier.marketing.store.app.test.executor.TransactionalExe;
import com.aircraftcarrier.marketing.store.client.TestService;
import com.aircraftcarrier.marketing.store.common.LoginUserInfo;
import com.aircraftcarrier.marketing.store.domain.drools.KieTemplate;
import com.aircraftcarrier.marketing.store.domain.drools.KieUtils;
import com.aircraftcarrier.marketing.store.domain.event.AccountEvent;
import com.aircraftcarrier.marketing.store.domain.model.test.Address;
import com.aircraftcarrier.marketing.store.domain.model.test.Sale;
import com.aircraftcarrier.marketing.store.infrastructure.config.reload.ReloadDroolsRules;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author lzp
 */
@Slf4j
@Service
public class TestServiceImpl implements TestService {
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;
    @Resource
    private TransactionalExe transactionalExe;
    @Resource
    private KieTemplate kieTemplate;
    @Resource
    private ReloadDroolsRules reloadDroolsRules;

    @Override
    public void testTransactional() {
        transactionalExe.execute();
    }

    @Override
    public void publishEvent() {
        LoginUserInfo loginUserInfo = new LoginUserInfo();
        loginUserInfo.setUserName("6409825@qq.com");
        applicationEventPublisher.publishEvent(new AccountEvent<>(loginUserInfo));
    }

    @Override
    public String testLock(Serializable id) {
        LockUtil.lock(id);
        try {

            int s = 0;
            do {
                s++;
                TimeUnit.SECONDS.sleep(1);
                System.out.println("计时：" + s);
            } while (s < 25);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            LockUtil.unLock();
        }
        return "success";
    }

    @Override
    public void applyDiscount(Map<String, Object> params) {
        Sale sale = ObjUtil.map2Obj(params, Sale.class);
        kieTemplate.execute(sale);
        log.info("执行规则后返回 sale: {}", JSON.toJSONString(sale));

        Address address = ObjUtil.map2Obj(params, Address.class);
        kieTemplate.execute(address);
        log.info("执行规则后返回 address: {}", JSON.toJSONString(address));


        KieUtils.updateToVersion(ReloadDroolsRules.content);

        kieTemplate.execute(sale);
        log.info("执行规则后返回 sale2: {}", JSON.toJSONString(sale));

        kieTemplate.execute(address);
        log.info("执行规则后返回 address2: {}", JSON.toJSONString(address));

    }
}