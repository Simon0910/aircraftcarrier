package com.aircraftcarrier.marketing.store.app.message.consumer.group2handler;

import com.aircraftcarrier.framework.message.taghandler.AbstractRocketMQGroupTagHandler;
import com.aircraftcarrier.marketing.store.domain.message.ConsumerGroup;
import com.aircraftcarrier.marketing.store.domain.message.Tag;
import com.aircraftcarrier.marketing.store.domain.message.event.CartItemEvent;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Group2CardItemDelMessageHandler extends AbstractRocketMQGroupTagHandler<CartItemEvent> {

    @Override
    public String handlerGroup() {
        return ConsumerGroup.my_group_2;
    }

    @Override
    public String tag() {
        return Tag.cart_item_del;
    }

    @Override
    public boolean idempotentResult(CartItemEvent cartItemEvent) throws Exception {
        return super.idempotentResult(cartItemEvent);
    }

    @Override
    public void doHandle(CartItemEvent cartItemEvent) throws Exception {
        log.info("group 2 cart_item_del " + JSON.toJSONString(cartItemEvent));
        // Thread.sleep(1000);
        // throw new NeedRetryException("haha");
        log.info("group 2 cart_item_del end");
    }

    @Override
    public void onException(CartItemEvent cartItemEvent, Exception e) throws Exception {

    }

}