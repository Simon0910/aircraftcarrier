package com.aircraftcarrier.framework.model.response;

import com.aircraftcarrier.framework.exception.ErrorCode;
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

    private SingleResponse(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static <T> SingleResponse<T> ok() {
        return new SingleResponse<>();
    }

    public static <T> SingleResponse<T> ok(T data) {
        return new SingleResponse<>(data);
    }

    public static <T> SingleResponse<T> error() {
        return error(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    public static <T> SingleResponse<T> error(int code) {
        return error(code, "");
    }

    public static <T> SingleResponse<T> error(String msg) {
        return error(ErrorCode.INTERNAL_SERVER_ERROR, msg);
    }

    public static <T> SingleResponse<T> error(int code, String msg) {
        return new SingleResponse<>(code, msg);
    }


    public T getData() {
        return data;
    }
}
