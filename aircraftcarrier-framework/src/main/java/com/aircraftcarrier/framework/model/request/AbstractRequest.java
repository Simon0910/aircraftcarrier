package com.aircraftcarrier.framework.model.request;

import lombok.Getter;
import lombok.Setter;

/**
 * @author lzp
 */
@Getter
@Setter
public abstract class AbstractRequest {

    private static final long serialVersionUID = 1L;

    /**
     * 请求标识
     */
    private String requestId;

    /**
     * 当前操作人
     */
    private String operator;
}
