package com.aircraftcarrier.marketing.store.app.message.producer;

import com.aircraftcarrier.framework.message.Message;
import com.aircraftcarrier.marketing.store.domain.message.Tag;
import com.aircraftcarrier.marketing.store.domain.message.event.CartItemEvent;
import com.aircraftcarrier.marketing.store.domain.message.producer.RmqProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * MessageProducer
 *
 * @author zhipengliu
 * @date 2024/7/7
 * @since 1.0
 */
@Service
public class MessageProducer {

    @Autowired
    RmqProducer rmqProducer;

    public String sendCardItemAddEvent(CartItemEvent event) throws Exception {
        return rmqProducer.syncSendOrderly(new Message<>(Tag.cart_item_add, event, event.getOrderId()));
    }

    public String sendCardItemDelEvent(CartItemEvent event) throws Exception {
        return rmqProducer.syncSendOrderly(new Message<>(Tag.cart_item_del, event, event.getOrderId()));
    }
}
