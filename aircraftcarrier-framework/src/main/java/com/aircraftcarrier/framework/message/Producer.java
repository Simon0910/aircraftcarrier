package com.aircraftcarrier.framework.message;

/**
 * Producer
 *
 * @author zhipengliu
 * @date 2024/7/6
 * @since 1.0
 */
public interface Producer {
    String syncSendOrderly(Message<?> message) throws Exception;
}
