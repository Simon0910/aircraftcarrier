package com.aircraftcarrier.marketing.store.adapter.web.message.producer;

import com.aircraftcarrier.framework.message.Message;
import com.aircraftcarrier.framework.message.Producer;
import com.aircraftcarrier.framework.tookit.JsonUtil;
import com.aircraftcarrier.marketing.store.adapter.web.message.Tag;
import com.aircraftcarrier.marketing.store.adapter.web.message.event.CartItemEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * RmqProducer
 *
 * @author zhipengliu
 * @date 2024/7/6
 * @since 1.0
 */
@Slf4j
@Service
public class RmqProducer implements Producer {

    @Value("${rocketmq.topic}")
    private String my_rocketmq_topic;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public String send(Message<?> message) throws Exception {
        log.info("sync send: {}", JsonUtil.toJsonString(message));
        rocketMQTemplate.send(message.getDestination(), MessageBuilder.withPayload(message).build());
        return "OK";
    }

    public String sendCardItemAddEvent(CartItemEvent event) throws Exception {
        return send(new Message<>(my_rocketmq_topic, Tag.cart_item_add, event));
    }
}
