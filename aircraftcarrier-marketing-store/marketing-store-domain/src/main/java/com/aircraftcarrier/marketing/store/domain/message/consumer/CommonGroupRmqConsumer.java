package com.aircraftcarrier.marketing.store.domain.message.consumer;

import com.aircraftcarrier.framework.message.listener.AbstractRocketMQTagListener;
import com.aircraftcarrier.framework.message.taghandler.AbstractRocketMQGroupTagHandler;
import com.aircraftcarrier.marketing.store.domain.message.ConsumerGroup;
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
        , consumerGroup = ConsumerGroup.my_common_group
        , consumeMode = ConsumeMode.ORDERLY
        , maxReconsumeTimes = 16 // 最大重试次数
)
public class CommonGroupRmqConsumer extends AbstractRocketMQTagListener {

    public CommonGroupRmqConsumer(Map<String, AbstractRocketMQGroupTagHandler<?>> rmqMessageTagHandlerMap) {
        super(ConsumerGroup.my_common_group, rmqMessageTagHandlerMap);
    }
}
