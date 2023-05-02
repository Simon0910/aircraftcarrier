package com.aircraftcarrier.framework.model.response;

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

    private MultiResponse(int code, String msg, String detailMessage) {
        this.code = code;
        this.msg = msg;
        this.detailMessage = detailMessage;
    }

    public static <T> MultiResponse<T> ok() {
        return ok(Collections.emptyList());
    }

    public static <T> MultiResponse<T> ok(Collection<T> data) {
        return new MultiResponse<>(data);
    }

    public static <T> MultiResponse<T> error(int code, String msg) {
        return error(code, msg, "");
    }

    public static <T> MultiResponse<T> error(int code, String msg, String detailMessage) {
        return new MultiResponse<>(code, msg, detailMessage);
    }

    public Collection<T> getData() {
        return data;
    }

}
