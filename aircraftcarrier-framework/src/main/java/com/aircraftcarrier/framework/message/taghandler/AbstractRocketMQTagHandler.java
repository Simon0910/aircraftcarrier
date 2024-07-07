package com.aircraftcarrier.framework.message.taghandler;

import com.aircraftcarrier.framework.message.Message;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * AbstractRmqMessageTagHandler
 *
 * @author zhipengliu
 * @date 2024/7/7
 * @since 1.0
 */
@Slf4j
public abstract class AbstractRocketMQTagHandler<T> {

    private final TypeReference<T> typeReference;
    private final Class<T> tClass;

    protected AbstractRocketMQTagHandler() {
        Type actualTypeArgument = ((ParameterizedType) AbstractRocketMQTagHandler.this.getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];

        this.tClass = (Class<T>) actualTypeArgument;

        this.typeReference = new TypeReference<T>() {
            @Override
            public Type getType() {
                return actualTypeArgument;
            }
        };
    }

    public Class<T> getTargetClass() {
        return tClass;
    }

    public T getTarget(Message<?> message) {
        return JSON.parseObject(JSON.toJSONString(message.getMsg()), typeReference.getType());
    }

    public void handle(Message<?> message) throws Exception {
        doHandle(getTarget(message));
    }

    public void exception(Message<?> message, Exception e) throws Exception {
        onException(getTarget(message), e);
    }

    public abstract String tag();

    public abstract void doHandle(T t) throws Exception;

    public abstract void onException(T t, Exception e) throws Exception;

}
