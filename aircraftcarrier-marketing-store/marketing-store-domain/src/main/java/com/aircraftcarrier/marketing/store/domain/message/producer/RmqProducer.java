package com.aircraftcarrier.marketing.store.domain.message.producer;

import com.aircraftcarrier.framework.message.Message;
import com.aircraftcarrier.framework.message.Producer;
import com.aircraftcarrier.framework.tookit.JsonUtil;
import com.aircraftcarrier.marketing.store.domain.message.Topic;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private Topic topic;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public String syncSendOrderly(Message<?> message) throws Exception {
        log.info("syncSendOrderly start businessId: {}, {}", message.getBusinessId(), JsonUtil.toJsonString(message));

        String destination = topic.getMy_rocketmq_topic() + ":" + message.getTag();

        SendResult sendResult = rocketMQTemplate.syncSendOrderly(
                destination
                , MessageBuilder.withPayload(message)
                        // keys：topic下的消息索引
                        .setHeader(RocketMQHeaders.KEYS, message.getKey())
                        .build()
                // 根据businessId有序
                , message.getBusinessId()
        );

        String msgId = sendResult.getMsgId();
        message.setId(msgId);
        log.info("syncSendOrderly end businessId: {}, msgId: {}", message.getBusinessId(), msgId);
        return msgId;
    }


}
