package com.aircraftcarrier.marketing.store.adapter.web.message;

import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.marketing.store.client.MessageService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MessageController
 *
 * @author zhipengliu
 * @date 2024/7/6
 * @since 1.0
 */
@RequestMapping(value = "/web/message/")
@RestController
public class MessageController {

    @Autowired
    MessageService messageService;

    @ApiOperation("send")
    @GetMapping("/send")
    public SingleResponse<String> send() throws Exception {
        return messageService.send();
    }

    @ApiOperation("sendDelay")
    @GetMapping("/sendDelay")
    public SingleResponse<String> sendDelay() throws Exception {
        return messageService.sendDelay();
    }
}
