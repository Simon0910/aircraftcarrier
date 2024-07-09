package com.aircraftcarrier.marketing.store.client;

import com.aircraftcarrier.framework.model.response.SingleResponse;

/**
 * MessageService
 *
 * @author zhipengliu
 * @date 2024/7/7
 * @since 1.0
 */
public interface MessageService {
    SingleResponse<String> send() throws Exception;

    SingleResponse<String> sendDelay() throws Exception;
}
