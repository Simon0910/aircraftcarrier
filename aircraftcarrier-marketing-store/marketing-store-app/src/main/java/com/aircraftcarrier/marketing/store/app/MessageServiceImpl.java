package com.aircraftcarrier.marketing.store.app;

import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.marketing.store.app.message.producer.MessageProducer;
import com.aircraftcarrier.marketing.store.client.MessageService;
import com.aircraftcarrier.marketing.store.domain.message.event.CartItemEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * MessageService
 *
 * @author zhipengliu
 * @date 2024/7/7
 * @since 1.0
 */
@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    MessageProducer messageProducer;

    @Override
    public SingleResponse<String> send() throws Exception {
        String msgId = messageProducer.sendCardItemAddEvent(new CartItemEvent("orderId_001", "itemId_1", 1));
        String msgId2 = messageProducer.sendCardItemDelEvent(new CartItemEvent("orderId_001", "itemId_1", 1));
        return SingleResponse.ok(msgId);
    }
}
