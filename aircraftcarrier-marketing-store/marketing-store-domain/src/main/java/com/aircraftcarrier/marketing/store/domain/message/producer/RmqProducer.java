package com.aircraftcarrier.marketing.store.domain.message.producer;

import com.aircraftcarrier.framework.message.Message;
import com.aircraftcarrier.framework.message.Producer;
import com.aircraftcarrier.framework.tookit.JsonUtil;
import com.aircraftcarrier.marketing.store.domain.message.Topic;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.jetbrains.annotations.NotNull;
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

    @NotNull
    private String getDestination(Message<?> message) {
        String destination;
        if (StringUtils.isBlank(message.getTopic())) {
            destination = topic.getMy_rocketmq_topic() + ":" + message.getTag();
        } else {
            destination = message.getTopic() + ":" + message.getTag();
        }
        return destination;
    }

    @Override
    public String syncSend(Message<?> message) throws Exception {
        log.info("syncSend start message: {}", JsonUtil.toJsonString(message));

        SendResult sendResult = rocketMQTemplate.syncSend(
                // destination
                getDestination(message)
                // msg
                , MessageBuilder.withPayload(message)
                        // keys：topic下的消息索引
                        .setHeader(RocketMQHeaders.KEYS, message.getKey())
                        .build()
        );

        String msgId = sendResult.getMsgId();
        log.info("syncSend end, msgId: {}", msgId);
        return msgId;
    }

    @Override
    public String syncSendOrderly(Message<?> message, String orderly) throws Exception {
        log.info("syncSendOrderly start orderly: {}, {}", orderly, JsonUtil.toJsonString(message));

        SendResult sendResult = rocketMQTemplate.syncSendOrderly(
                // destination
                getDestination(message)
                // msg
                , MessageBuilder.withPayload(message)
                        // keys：topic下的消息索引
                        .setHeader(RocketMQHeaders.KEYS, message.getKey())
                        .build()
                // 根据orderly有序
                , orderly);

        String msgId = sendResult.getMsgId();
        log.info("syncSendOrderly end orderly: {}, msgId: {}", orderly, msgId);
        return msgId;
    }

    @Override
    public void asyncSend(Message<?> message, SendCallback sendCallback) {
        log.info("asyncSend start key: {}, {}", message.getKey(), JsonUtil.toJsonString(message));

        rocketMQTemplate.asyncSend(
                // destination
                getDestination(message)
                // msg
                , MessageBuilder.withPayload(message)
                        // keys：topic下的消息索引
                        .setHeader(RocketMQHeaders.KEYS, message.getKey())
                        .build()
                // callback
                , sendCallback);

        log.info("asyncSend end key: {} ", message.getKey());
    }


    @Override
    public void sendOneWay(Message<?> message) {
        log.info("sendOneWay start key: {}, {}", message.getKey(), JsonUtil.toJsonString(message));

        rocketMQTemplate.sendOneWay(
                // destination
                getDestination(message)
                // msg
                , MessageBuilder.withPayload(message)
                        // keys：topic下的消息索引
                        .setHeader(RocketMQHeaders.KEYS, message.getKey())
                        .build());

        log.info("sendOneWay end key: {} ", message.getKey());
    }

    @Override
    public String syncSendDelayLevel(Message<?> message, int delayLevel) {
        log.info("syncSend start message: {}", JsonUtil.toJsonString(message));

        SendResult sendResult = rocketMQTemplate.syncSend(
                // destination
                getDestination(message)
                // msg
                , MessageBuilder.withPayload(message)
                        // keys：topic下的消息索引
                        .setHeader(RocketMQHeaders.KEYS, message.getKey())
                        .build()
                // timeout
                , 3000
                // delayLevel: [1-18]
                // 1   2   3    4    5   6   7   8   9  10   11  12  13  14   15   16   17  18
                // 1s, 5s, 10s, 30s, 1m, 2m, 3m, 4m, 5m, 6m, 7m, 8m, 9m, 10m, 20m, 30m, 1h, 2h
                , delayLevel);

        String msgId = sendResult.getMsgId();
        log.info("syncSend end, msgId: {}", msgId);
        return msgId;
    }


}
