package com.aircraftcarrier.framework.model.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author lzp
 */
@Setter
@Getter
public class Response implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * 追踪id
     */
    protected String responseId;

    /**
     * 编码：0表示成功，其他值表示失败
     */
    @ApiModelProperty(value = "编码：0表示成功，其他值表示失败", required = true, example = "0")
    protected int code = 0;
    /**
     * 消息内容
     */
    @ApiModelProperty(value = "消息内容", required = true, example = "success")
    protected String msg = "success";

    /**
     * 是否成功
     *
     * @return 成功表示
     */
    public boolean success() {
        return code == 0;
    }
}
