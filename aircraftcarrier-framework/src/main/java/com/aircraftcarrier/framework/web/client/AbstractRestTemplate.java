package com.aircraftcarrier.framework.web.client;

import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

/**
 * @author liuzhipeng
 * @since 2020-01-15 10:13
 */
@AllArgsConstructor
public abstract class AbstractRestTemplate implements RestOperations {

    /**
     * 实现RestOperations所有的接口
     */
    @Delegate
    protected RestTemplate restTemplate;
}