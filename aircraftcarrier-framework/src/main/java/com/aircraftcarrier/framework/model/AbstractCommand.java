package com.aircraftcarrier.framework.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Command request from Client.
 *
 * @author Frank Zhang 2020.11.13
 */
@Getter
@Setter
public abstract class AbstractCommand implements Serializable {

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
     * 数据版本号
     */
    private Long version;

    /**
     * initCreate
     */
    public void initCreate(String operator) {
        this.operator = operator;
    }

    /**
     * initUpdate
     */
    public void initUpdate(String operator) {
        this.operator = operator;
    }
}
