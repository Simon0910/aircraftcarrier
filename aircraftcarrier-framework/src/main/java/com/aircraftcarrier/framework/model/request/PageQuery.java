package com.aircraftcarrier.framework.model.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * Page Query Param
 *
 * @author jacky
 */
@Getter
@Setter
public class PageQuery extends AbstractRequest {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * 当前页码，下标从1开始
     */
    @NotNull(message = "不能为null")
    @Range(min = 1, message = "最小1")
    @ApiModelProperty(value = "当前页码, 下标从1开始", required = true, example = "1")
    private Integer pageNum;

    /**
     * 每页的数量
     */
    @NotNull(message = "不能为null")
    @ApiModelProperty(value = "每页的数量, 最大值1000", required = true, example = "10")
    @Range(min = 1, max = 1000, message = "最小1,最大1000")
    private Integer pageSize;

}
