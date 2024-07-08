package com.aircraftcarrier.framework.message;

import com.aircraftcarrier.framework.support.trace.TraceIdUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

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
     * 由rocketMQ生成唯一ID
     */
    private String id;

    /**
     * 对应msg的handler处理类
     */
    private String tag;

    /**
     * 用于查询topic下的消息， rocketmq根据此id索引
     * 可以是数据库主键，方便后续追踪
     */
    private String key;

    /**
     * handler处理类接受的msg
     */
    private T msg;

    /**
     * use this key to select queue. for example: orderId, productId ...
     * 根据此id，消息有序
     */
    private String businessId;


    public Message() {
    }


    public Message(String tag, T msg, String businessId) {
        this.tag = tag;
        this.msg = msg;
        this.businessId = businessId;
        this.key = TraceIdUtil.getTraceIdOrUuid();
    }


}
