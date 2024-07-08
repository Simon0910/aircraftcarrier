package com.aircraftcarrier.framework.message;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Message
 *
 * @author zhipengliu
 * @date 2024/7/6
 * @since 1.0
 */
@Getter
@Setter
public class Message<T extends Serializable> implements Serializable {
    /**
     * serialVersionUID
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 对应msg的handler处理类
     */
    private String tag;
    /**
     * 用于查询topic下的消息， rocketmq根据此id索引
     */
    private String key = UUID.randomUUID().toString();
    /**
     * handler处理类接受的msg
     */
    private T msg;
    /**
     * msg的业务id，例如：orderId
     * 根据此id，消息有序
     */
    private String businessId;

    public Message() {
    }

    public Message(String tag, T msg, String businessId) {
        this.tag = tag;
        this.msg = msg;
        this.businessId = businessId;
    }


}
