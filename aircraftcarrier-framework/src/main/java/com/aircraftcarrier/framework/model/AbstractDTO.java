package com.aircraftcarrier.framework.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Data Transfer object, including Command, Query and Response,
 * <p>
 * Command and Query is CQRS concept.
 *
 * @author Frank Zhang 2020.11.13
 */
@Setter
@Getter
public abstract class AbstractDTO<T> implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * 请求标识
     */
    protected String requestId;

    /**
     * 当前操作人
     */
    protected String operator;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 修改人
     */
    private String updateUser;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

}
