package com.aircraftcarrier.marketing.store.adapter.web.message.consumer.handler;

import com.aircraftcarrier.framework.exception.NeedRetryException;
import com.aircraftcarrier.framework.message.taghandler.AbstractRocketMQTagHandler;
import com.aircraftcarrier.marketing.store.adapter.web.message.Tag;
import com.aircraftcarrier.marketing.store.adapter.web.message.event.CartItemEvent;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CardItemAddMessageHandler extends AbstractRocketMQTagHandler<CartItemEvent> {

    @Override
    public String tag() {
        return Tag.cart_item_add;
    }

    @Override
    public void doHandle(CartItemEvent cartItemEvent) throws Exception {
        // 业务幂等处理
        System.out.println("cart_item_add " + JSON.toJSONString(cartItemEvent));
        // Thread.sleep(1000);
        throw new NeedRetryException("haha");
        // System.out.println("cart_item_add end");
    }

    @Override
    public void onException(CartItemEvent cartItemEvent, Exception e) throws Exception {

    }

}