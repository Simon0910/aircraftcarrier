package com.aircraftcarrier.framework.message.listener;

import com.aircraftcarrier.framework.exception.NeedRetryException;
import com.aircraftcarrier.framework.message.Message;
import com.aircraftcarrier.framework.message.taghandler.AbstractRocketMQGroupTagHandler;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQListener;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * AbstractRocketMQListener
 *
 * @author zhipengliu
 * @date 2024/7/7
 * @since 1.0
 */
@Slf4j
public abstract class AbstractRocketMQTagListener<T extends Serializable> implements RocketMQListener<Message<T>> {

    private final String selectedHandlerGroup;
    private Map<String, AbstractRocketMQGroupTagHandler<?>> rmqMessageTagHandlerMap;

    public AbstractRocketMQTagListener(String selectedHandlerGroup, Map<String, AbstractRocketMQGroupTagHandler<?>> rmqMessageTagHandlerMap) {
        this.selectedHandlerGroup = selectedHandlerGroup;
        this.rmqMessageTagHandlerMap = rmqMessageTagHandlerMap;
    }

    @PostConstruct
    public void init() {
        Map<String, AbstractRocketMQGroupTagHandler<?>> customMap = HashMap.newHashMap(rmqMessageTagHandlerMap.size());
        for (Map.Entry<String, AbstractRocketMQGroupTagHandler<?>> entry : rmqMessageTagHandlerMap.entrySet()) {
            if (selectedHandlerGroup.equals(entry.getValue().handlerGroup())) {
                String tag = entry.getValue().tag();
                AbstractRocketMQGroupTagHandler<?> abstractRmqMessageTagHandler = customMap.get(tag);
                if (abstractRmqMessageTagHandler != null) {
                    throw new RuntimeException("handlerGroup [" + selectedHandlerGroup + "] exist conflicting tags [" + tag + "]");
                }
                customMap.put(tag, entry.getValue());
            }
        }
        rmqMessageTagHandlerMap = customMap;
    }


    @Override
    public void onMessage(Message<T> message) {
        log.info("rocket message: {}", JSON.toJSONString(message));
        // route
        AbstractRocketMQGroupTagHandler<?> abstractRmqMessageTagHandler = rmqMessageTagHandlerMap.get(message.getTag());

        try {
            if (abstractRmqMessageTagHandler != null) {
                abstractRmqMessageTagHandler.handle(message);
            } else {
                log.error("tag [{}] handler is not found", message.getTag());
            }
        } catch (NeedRetryException e) {
            // 消费失败需要重试
            log.error("onMessage NeedRetryException: ", e);
            throw e;
        } catch (InterruptedException e) {
            // 消费失败需要重试
            log.error("onMessage InterruptedException: ", e);
            Thread.currentThread().interrupt();
            throw new NeedRetryException("-1", "消费失败需要重试", e);
        } catch (Exception e) {
            // 记录数据库 或者 发送到异常队列 告警并统一处理，并且不重试
            log.error("onMessage error: ", e);
            try {
                abstractRmqMessageTagHandler.exception(message, e);
            } catch (Exception ex) {
                log.error("onMessage ex: ", ex);
            }
        }
        log.info("rocket message end.");
    }


}
