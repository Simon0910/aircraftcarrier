package com.aircraftcarrier.marketing.store.domain.message;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Topic
 *
 * @author zhipengliu
 * @date 2024/7/8
 * @since 1.0
 */
@Setter
@Getter
@Component
public class Topic {
    @Value("${rocketmq.topic}")
    private String my_rocketmq_topic;
}
