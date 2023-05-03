package com.aircraftcarrier.framework.model.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 响应数据
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@ApiModel(value = "结果响应")
public class SingleResponse<T> extends Response {
    /**
     * 响应数据
     */
    @ApiModelProperty(value = "响应数据", required = true, example = "null")
    private T data;

    private SingleResponse() {
    }

    private SingleResponse(T data) {
        this.data = data;
    }

    private SingleResponse(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private SingleResponse(String code, String msg, String detailMessage) {
        this.code = code;
        this.msg = msg;
        this.detailMessage = detailMessage;
    }

    public static <T> SingleResponse<T> ok() {
        return new SingleResponse<>();
    }

    public static <T> SingleResponse<T> ok(T data) {
        return new SingleResponse<>(data);
    }

    public static <T> SingleResponse<T> error(String code, String msg) {
        return error(code, msg, "");
    }

    public static <T> SingleResponse<T> error(String code, String msg, String detailMessage) {
        return new SingleResponse<>(code, msg, detailMessage);
    }


    public T getData() {
        return data;
    }
}
