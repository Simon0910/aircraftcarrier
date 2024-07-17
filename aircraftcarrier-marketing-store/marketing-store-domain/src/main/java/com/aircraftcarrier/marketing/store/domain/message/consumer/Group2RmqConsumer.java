package com.aircraftcarrier.marketing.store.domain.message.consumer;

import com.aircraftcarrier.framework.message.listener.AbstractRocketMQTagListener;
import com.aircraftcarrier.framework.message.taghandler.AbstractRocketMQGroupTagHandler;
import com.aircraftcarrier.marketing.store.domain.message.ConsumerGroup;
import com.aircraftcarrier.marketing.store.domain.message.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;

import java.util.Map;

/**
 * Group1RmqConsumer
 *
 * @author zhipengliu
 * @date 2024/7/8
 * @since 1.0
 */
@Slf4j
// @Component
@RocketMQMessageListener(topic = "${rocketmq.topic}"
        , consumerGroup = ConsumerGroup.my_group_2
        , consumeMode = ConsumeMode.ORDERLY
        , maxReconsumeTimes = 16 // 最大重试次数
        , selectorExpression = Tag.cart_item_add + "||" + Tag.cart_item_del
)
public class Group2RmqConsumer extends AbstractRocketMQTagListener {

    public Group2RmqConsumer(Map<String, AbstractRocketMQGroupTagHandler<?>> rmqMessageTagHandlerMap) {
        super(ConsumerGroup.my_group_2, rmqMessageTagHandlerMap);
    }
}