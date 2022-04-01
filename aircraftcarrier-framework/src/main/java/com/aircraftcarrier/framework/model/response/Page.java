package com.aircraftcarrier.framework.model.response;

import com.aircraftcarrier.framework.tookit.BeanUtil;
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
public class Page<R extends Serializable> implements Serializable {
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
    private List<R> list;

    public Page(List<R> list, long total) {
        this.list = list;
        this.total = total;
    }


    public static <R extends Serializable> Page<R> build(List<R> list, long total) {
        return new Page<>(list, total);
    }

    public static <R extends Serializable, T> Page<R> build(List<T> list, long total, Class<R> targetClass) {
        return new Page<>(BeanUtil.convertList(list, targetClass), total);
    }

}
