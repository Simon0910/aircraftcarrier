package com.aircraftcarrier.framework.cache.config;

import com.aircraftcarrier.framework.cache.suport.MyLockTemplate;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.executor.LockExecutor;
import com.baomidou.lock.spring.boot.autoconfigure.Lock4jProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.List;

/**
 * 分布式锁自动配置器
 *
 * @author zengzhihong TaoYu
 */
@ConditionalOnClass(LockTemplate.class)
@Configuration
public class LockTemplateAutoConfiguration {

    @Resource
    Lock4jProperties properties;

    @Bean
    public LockTemplate lockTemplate(List<LockExecutor> executors) {
        LockTemplate lockTemplate = new MyLockTemplate(properties);
        lockTemplate.setExecutors(executors);
        return lockTemplate;
    }
}
