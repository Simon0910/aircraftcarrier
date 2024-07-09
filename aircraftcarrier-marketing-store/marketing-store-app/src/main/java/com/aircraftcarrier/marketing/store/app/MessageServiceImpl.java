package com.aircraftcarrier.marketing.store.app;

import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.marketing.store.app.message.producer.MessageProducer;
import com.aircraftcarrier.marketing.store.client.MessageService;
import com.aircraftcarrier.marketing.store.domain.message.event.CartItemEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * MessageService
 *
 * @author zhipengliu
 * @date 2024/7/7
 * @since 1.0
 */
@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    MessageProducer messageProducer;

    @Override
    public SingleResponse<String> send() throws Exception {
        String msgId = messageProducer.sendCardItemAddEvent(new CartItemEvent("orderId_001", "itemId_1", 1));
        log.info("msgId1");
        String msgId2 = messageProducer.sendCardItemDelEvent(new CartItemEvent("orderId_001", "itemId_2", 1));
        log.info("msgId2");
        String msgId3 = messageProducer.sendCardItemAddEvent(new CartItemEvent("orderId_002", "itemId_3", 1));
        log.info("msgId3");
        String msgId4 = messageProducer.sendCardItemDelEvent(new CartItemEvent("orderId_002", "itemId_4", 1));
        log.info("msgId4");
        return SingleResponse.ok(msgId);
    }

    @Override
    public SingleResponse<String> sendDelay() throws Exception {
        String msgId5 = messageProducer.sendCardItemDelayDelEvent(new CartItemEvent("orderId_002", "itemId_4", 1));
        log.info("msgId5");
        return SingleResponse.ok(msgId5);
    }
}
