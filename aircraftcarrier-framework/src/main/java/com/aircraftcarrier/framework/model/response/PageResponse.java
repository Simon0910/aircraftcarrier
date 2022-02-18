package com.aircraftcarrier.framework.model.response;

import com.aircraftcarrier.framework.tookit.BeanUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author lzp
 */
@Getter
@Setter
@ApiModel(value = "分页结果响应")
public class PageResponse<R extends Serializable> extends Response {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * 总条数
     */
    @ApiModelProperty(value = "总条数", required = true, example = "1")
    private long total;

    /**
     * 响应数据
     */
    @ApiModelProperty(value = "响应数据", required = true, example = "[]")
    private List<R> data;

    public PageResponse(List<R> data, long total) {
        this.data = data;
        this.total = total;
    }


    public static <R extends Serializable> PageResponse<R> build(List<R> data, long total) {
        return new PageResponse<>(data, total);
    }

    public static <R extends Serializable, T> PageResponse<R> build(List<T> data, long total, Class<R> targetClass) {
        return new PageResponse<>(BeanUtils.convertList(data, targetClass), total);
    }

}
