package com.aircraftcarrier.framework.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * 类注释内容
 *
 * @author zhipengliu
 * @date 2023/7/22
 * @since 1.0
 */
@Slf4j
public class ApplicationEventAllListener implements ApplicationListener {

    /**
     * 应用事件
     *
     * @param event ApplicationEvent
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        Class<? extends ApplicationEvent> eventClass = event.getClass();
        log.info("应用事件: " + eventClass.getSimpleName());
    }
}
