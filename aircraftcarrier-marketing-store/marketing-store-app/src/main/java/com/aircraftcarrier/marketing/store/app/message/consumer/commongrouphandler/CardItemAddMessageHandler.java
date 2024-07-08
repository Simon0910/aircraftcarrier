package com.aircraftcarrier.marketing.store.app.message.consumer.commongrouphandler;

import com.aircraftcarrier.framework.message.taghandler.AbstractRocketMQGroupTagHandler;
import com.aircraftcarrier.marketing.store.domain.message.ConsumerGroup;
import com.aircraftcarrier.marketing.store.domain.message.Tag;
import com.aircraftcarrier.marketing.store.domain.message.event.CartItemEvent;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CardItemAddMessageHandler extends AbstractRocketMQGroupTagHandler<CartItemEvent> {

    @Override
    public String handlerGroup() {
        return ConsumerGroup.my_common_group;
    }

    @Override
    public String tag() {
        return Tag.cart_item_add;
    }

    @Override
    public boolean idempotentResult(CartItemEvent cartItemEvent) throws Exception {
        return super.idempotentResult(cartItemEvent);
    }

    @Override
    public void doHandle(CartItemEvent cartItemEvent) throws Exception {
        log.info("common group cart_item_add " + JSON.toJSONString(cartItemEvent));
        // Thread.sleep(1000);
        // throw new NeedRetryException("haha");
        log.info("common group cart_item_add end");
    }

    @Override
    public void onException(CartItemEvent cartItemEvent, Exception e) throws Exception {

    }

}