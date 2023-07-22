package com.aircraftcarrier.framework.support;


/**
 * 类注释内容
 *
 * @author zhipengliu
 * @date 2023/7/22
 * @since 1.0
 */

import com.aircraftcarrier.framework.cache.LockUtil2;
import com.aircraftcarrier.framework.tookit.ApplicationContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.AvailabilityState;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

import java.util.Map;

/**
 * @author lzp
 */
@Slf4j
public class ApplicationEventListener {

    /**
     * 监听系统消息
     *
     * @param event 可用性事件
     */
    @EventListener
    public void onAvailabilityState(AvailabilityChangeEvent<? extends AvailabilityState> event) {
        AvailabilityState state = event.getState();
        if (state instanceof LivenessState) {
            if (LivenessState.CORRECT == state) {
                log.info("可用性事件 CORRECT");
            } else if (LivenessState.BROKEN == state) {
                log.info("可用性事件 BROKEN");
            }
        } else if (state instanceof ReadinessState) {
            if (ReadinessState.ACCEPTING_TRAFFIC == state) {
                // 注册消费mq，注册rpc，初始化懒加载...
                log.info("可用性事件 ACCEPTING_TRAFFIC");
                Map<String, ApplicationContextReadiness> closeBeans = ApplicationContextUtil.getBeansOfType(ApplicationContextReadiness.class);
                closeBeans.forEach((k, obj) -> obj.readiness());
                LockUtil2.init();
            } else if (ReadinessState.REFUSING_TRAFFIC == state) {
                log.info("可用性事件 REFUSING_TRAFFIC");
            }
        }
    }

    /**
     * 监听系统消息
     *
     * @param event 容器关闭事件
     */
    @EventListener
    public void onContextClosedEvent(ContextClosedEvent event) {
        // 完成线程池任务，清理相关动作...
        log.info("容器关闭事件: " + event.getClass().getSimpleName());
        Map<String, ApplicationContextClosedEvent> closeBeans = ApplicationContextUtil.getBeansOfType(ApplicationContextClosedEvent.class);
        closeBeans.forEach((k, obj) -> obj.contextClosed());
    }
}
