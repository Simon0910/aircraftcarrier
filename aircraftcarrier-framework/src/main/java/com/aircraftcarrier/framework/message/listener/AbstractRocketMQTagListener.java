package com.aircraftcarrier.framework.message.listener;

import com.aircraftcarrier.framework.exception.NeedRetryException;
import com.aircraftcarrier.framework.message.Message;
import com.aircraftcarrier.framework.message.taghandler.AbstractRocketMQGroupTagHandler;
import com.aircraftcarrier.framework.support.trace.TraceIdUtil;
import com.aircraftcarrier.framework.tookit.ObjectMapperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.core.RocketMQListener;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * AbstractRocketMQListener
 *
 * @author zhipengliu
 * @date 2024/7/7
 * @since 1.0
 */
@Slf4j
public abstract class AbstractRocketMQTagListener implements RocketMQListener<MessageExt> {
    private final String selectedHandlerGroup;
    private Map<String, AbstractRocketMQGroupTagHandler<?>> rmqMessageTagHandlerMap;

    protected AbstractRocketMQTagListener(String selectedHandlerGroup, Map<String, AbstractRocketMQGroupTagHandler<?>> rmqMessageTagHandlerMap) {
        this.selectedHandlerGroup = selectedHandlerGroup;
        this.rmqMessageTagHandlerMap = rmqMessageTagHandlerMap;
    }

    @PostConstruct
    public void init() {
        Map<String, AbstractRocketMQGroupTagHandler<?>> customMap = HashMap.newHashMap(rmqMessageTagHandlerMap.size());
        for (Map.Entry<String, AbstractRocketMQGroupTagHandler<?>> entry : rmqMessageTagHandlerMap.entrySet()) {
            if (selectedHandlerGroup.equals(entry.getValue().handlerGroup())) {
                String tag = entry.getValue().tag();
                AbstractRocketMQGroupTagHandler<?> abstractRmqMessageTagHandler = customMap.get(tag);
                if (abstractRmqMessageTagHandler != null) {
                    throw new RuntimeException("handlerGroup [" + selectedHandlerGroup + "] exist conflicting tags [" + tag + "]");
                }
                customMap.put(tag, entry.getValue());
            }
        }
        rmqMessageTagHandlerMap = customMap;
    }


    @Override
    public void onMessage(MessageExt messageExt) {
        TraceIdUtil.setTraceId(messageExt.getKeys());
        String body = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("onMessage start msgId: {}, message: {}", messageExt.getMsgId(), body);

        // route
        AbstractRocketMQGroupTagHandler<?> abstractRmqMessageTagHandler = null;
        Message<?> message = null;
        try {
            abstractRmqMessageTagHandler = rmqMessageTagHandlerMap.get(messageExt.getTags());
            if (abstractRmqMessageTagHandler == null) {
                log.error("tag [{}] handler is not found", messageExt.getTags());
                return;
            }
            message = ObjectMapperUtil.json2Obj(body, Message.class);
            if (message == null) {
                log.error("message is null");
                return;
            }

            abstractRmqMessageTagHandler.handle(message);
            log.info("onMessage end msgId:{}", messageExt.getMsgId());
        } catch (NeedRetryException e) {
            // 消费失败需要重试
            log.error("onMessage NeedRetryException: ", e);
            throw e;
        } catch (InterruptedException e) {
            // 消费失败需要重试
            log.error("onMessage InterruptedException: ", e);
            Thread.currentThread().interrupt();
            throw new NeedRetryException("-1", "消费失败需要重试", e);
        } catch (Exception e) {
            // 记录数据库 或者 发送到异常队列 告警并统一处理，并且不重试
            log.error("onMessage error: ", e);
            try {
                assert abstractRmqMessageTagHandler != null;
                abstractRmqMessageTagHandler.exception(message, e);
            } catch (Exception ex) {
                log.error("onMessage ex: ", ex);
            }
        } finally {
            TraceIdUtil.removeAll();
        }
    }


}
