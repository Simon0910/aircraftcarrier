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

    private static final String SUCCESS = "0";

    /**
     * 追踪id
     */
    protected String responseId;

    /**
     * 编码：0表示成功，其他值表示失败
     */
    @ApiModelProperty(value = "编码：0表示成功，其他值表示失败", required = true, example = "0")
    protected String code = SUCCESS;
    /**
     * 消息内容
     */
    @ApiModelProperty(value = "消息内容", required = true, example = "success")
    protected String msg = "success";

    /**
     * 错误详情
     */
    @ApiModelProperty(value = "错误详情", required = false, example = "param is valid")
    protected String detailMessage;

    /**
     * 是否成功
     *
     * @return 成功表示
     */
    public boolean success() {
        return SUCCESS.equals(code);
    }
}
