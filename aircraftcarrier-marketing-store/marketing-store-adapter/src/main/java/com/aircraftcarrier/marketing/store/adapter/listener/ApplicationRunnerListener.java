package com.aircraftcarrier.marketing.store.adapter.listener;

import lombok.extern.slf4j.Slf4j;
import org.burningwave.core.assembler.StaticComponentContainer;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * description
 *
 * @author ext.liuzhipeng7
 * @date 2022/12/27
 */
@Slf4j
@Order(99)
@Component
public class ApplicationRunnerListener implements ApplicationRunner {

    @Resource
    private Environment environment;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 启动成功后续动作对外提供服务， 注册消费者，注册rpc接口等。。。
        log.info("=============register start....==========");
        StaticComponentContainer.Modules.exportAllToAll();
        log.info("=============register finish=============");
    }
}
