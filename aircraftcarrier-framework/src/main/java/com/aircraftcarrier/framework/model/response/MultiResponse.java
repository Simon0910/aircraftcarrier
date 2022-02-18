package com.aircraftcarrier.framework.model.response;

import com.aircraftcarrier.framework.exception.ErrorCode;
import com.aircraftcarrier.framework.tookit.MessageUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Collection;
import java.util.Collections;

/**
 * @author lzp
 */
@ApiModel(value = "结果响应")
public class MultiResponse<T> extends Response {
    /**
     * 响应数据
     */
    @ApiModelProperty(value = "响应数据", required = true, example = "[]")
    private Collection<T> data;

    private MultiResponse(Collection<T> data) {
        this.data = data;
    }

    private MultiResponse(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static <T> MultiResponse<T> ok() {
        return new MultiResponse<>(Collections.emptyList());
    }

    public static <T> MultiResponse<T> ok(Collection<T> data) {
        return new MultiResponse<>(data);
    }

    public static <T> MultiResponse<T> error() {
        return new MultiResponse<>(ErrorCode.INTERNAL_SERVER_ERROR, MessageUtils.getMessage(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    public static <T> MultiResponse<T> error(int code) {
        return new MultiResponse<>(code, MessageUtils.getMessage(code));
    }

    public static <T> MultiResponse<T> error(String msg) {
        return new MultiResponse<>(ErrorCode.INTERNAL_SERVER_ERROR, msg);
    }

    public static <T> MultiResponse<T> error(int code, String msg) {
        return new MultiResponse<>(code, msg);
    }

    public Collection<T> getData() {
        return data;
    }

}
