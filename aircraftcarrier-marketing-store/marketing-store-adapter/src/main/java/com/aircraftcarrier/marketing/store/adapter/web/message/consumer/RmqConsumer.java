package com.aircraftcarrier.marketing.store.adapter.web.message.consumer;

import com.aircraftcarrier.framework.message.Message;
import com.aircraftcarrier.framework.message.listener.AbstractRocketMQListener;
import com.aircraftcarrier.framework.message.taghandler.AbstractRocketMQTagHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * RmqConsumer
 *
 * @author zhipengliu
 * @date 2024/7/6
 * @since 1.0
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "${rocketmq.topic}"
        , consumerGroup = "my-rocketmq-consumerGroup"
        , consumeMode = ConsumeMode.ORDERLY
        , maxReconsumeTimes = 16 // 最大重试次数
)
public class RmqConsumer extends AbstractRocketMQListener<Message<?>> {

    public RmqConsumer(Map<String, AbstractRocketMQTagHandler<?>> rmqMessageTagHandlerMap) {
        super(rmqMessageTagHandlerMap);
    }
}
