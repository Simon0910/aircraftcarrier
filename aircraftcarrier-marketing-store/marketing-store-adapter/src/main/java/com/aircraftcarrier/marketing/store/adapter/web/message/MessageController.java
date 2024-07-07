package com.aircraftcarrier.marketing.store.adapter.web.message;

import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.marketing.store.adapter.web.message.event.CartItemEvent;
import com.aircraftcarrier.marketing.store.adapter.web.message.producer.RmqProducer;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MessageController
 *
 * @author zhipengliu
 * @date 2024/7/6
 * @since 1.0
 */
@RestController
public class MessageController {

    @Autowired
    RmqProducer producer;

    @ApiOperation("send")
    @GetMapping("/send")
    public SingleResponse<String> send() throws Exception {
        CartItemEvent event = new CartItemEvent("itemId_1", 1);
        producer.sendCardItemAddEvent(event);
        return SingleResponse.ok("send ok");
    }
}
