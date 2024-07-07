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

    private String destination;
    private String topic;
    private String tag;
    // 建议为业务主键
    private String messageId = UUID.randomUUID().toString();
    private T msg;

    public Message() {
    }

    public Message(String topic, String tag, T msg) {
        this.topic = topic;
        this.tag = tag;
        this.destination = topic + ":" + tag;
        this.msg = msg;
    }


}
