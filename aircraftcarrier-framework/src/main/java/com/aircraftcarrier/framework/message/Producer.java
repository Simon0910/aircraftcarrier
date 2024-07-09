package com.aircraftcarrier.framework.message;

import org.apache.rocketmq.client.producer.SendCallback;

/**
 * Producer
 *
 * @author zhipengliu
 * @date 2024/7/6
 * @since 1.0
 */
public interface Producer {

    String syncSend(Message<?> message) throws Exception;

    String syncSendOrderly(Message<?> message, String orderly) throws Exception;

    void asyncSend(Message<?> message, SendCallback sendCallback);

    void sendOneWay(Message<?> message);

    String syncSendDelayLevel(Message<?> message, int delayLevel);
}
