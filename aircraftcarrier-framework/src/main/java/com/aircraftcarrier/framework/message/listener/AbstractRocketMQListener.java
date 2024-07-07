package com.aircraftcarrier.framework.message.listener;

import com.aircraftcarrier.framework.cache.LockUtil2;
import com.aircraftcarrier.framework.cache.RedisLocker;
import com.aircraftcarrier.framework.exception.NeedRetryException;
import com.aircraftcarrier.framework.message.Message;
import com.aircraftcarrier.framework.message.taghandler.AbstractRocketMQTagHandler;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQListener;

import javax.annotation.PostConstruct;
import java.io.Serializable;
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
public abstract class AbstractRocketMQListener<T extends Serializable> implements RocketMQListener<Message<T>> {

    private Map<String, AbstractRocketMQTagHandler<?>> rmqMessageTagHandlerMap;

    public AbstractRocketMQListener(Map<String, AbstractRocketMQTagHandler<?>> rmqMessageTagHandlerMap) {
        this.rmqMessageTagHandlerMap = rmqMessageTagHandlerMap;
    }

    @PostConstruct
    public void init() {
        Map<String, AbstractRocketMQTagHandler<?>> customMap = HashMap.newHashMap(rmqMessageTagHandlerMap.size());
        for (Map.Entry<String, AbstractRocketMQTagHandler<?>> entry : rmqMessageTagHandlerMap.entrySet()) {
            String tag = entry.getValue().tag();
            AbstractRocketMQTagHandler<?> abstractRmqMessageTagHandler = customMap.get(tag);
            if (abstractRmqMessageTagHandler != null) {
                throw new RuntimeException("exist conflicting tags [" + tag + "]");
            }
            customMap.put(tag, entry.getValue());
        }
        rmqMessageTagHandlerMap = customMap;
    }



    @Override
    public void onMessage(Message<T> message) {
        log.info("rocket message: {}", JSON.toJSONString(message));
        String destination = message.getDestination();
        String[] tempArr = destination.split(":", 2);
        String tags = "";
        if (tempArr.length > 1) {
            tags = tempArr[1];
        }

        // route
        AbstractRocketMQTagHandler<?> abstractRmqMessageTagHandler = rmqMessageTagHandlerMap.get(tags);

        try {
            if (abstractRmqMessageTagHandler != null) {
                RedisLocker redisLocker = LockUtil2.tryLock(message.getMessageId());
                if (redisLocker.isLocked()) {
                    try {
                        abstractRmqMessageTagHandler.handle(message);
                    } finally {
                        redisLocker.unLock();
                    }
                }
            } else {
                log.error("tag [{}] handler is not found", tags);
            }
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
                abstractRmqMessageTagHandler.exception(message, e);
            } catch (Exception ex) {
                log.error("onMessage ex: ", ex);
            }
        }
        log.info("rocket message end.");
    }


}
