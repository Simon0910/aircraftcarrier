package com.aircraftcarrier.marketing.store.client.common;

import com.aircraftcarrier.framework.model.request.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * 关键字查询
 * <p>
 * 默认 limit 20
 */
@ApiModel(value = "KeywordQry")
@Setter
@Getter
public class KeywordQry extends Query {

    /**
     * keyword
     */
    @ApiModelProperty(value = "关键字", required = false, example = "123")
    @Size(max = 50, message = "请输入关键字长度不大于50")
    private String keyword;

    /**
     * keyword
     */
    @ApiModelProperty(value = "关键字", required = false, example = "goodsNo")
    private String likeField;

    /**
     * pageSize
     */
    @ApiModelProperty(value = "每次查询条数", required = false, example = "20")
    @Size(max = 50, message = "查询条数不大于50")
    private Integer pageSize = 20;

    /**
     * lastId
     */
    @ApiModelProperty(value = "最后一条数据id", required = false, example = "0")
    private Long lastId;

    /**
     * tableName
     */
    @ApiModelProperty(value = "关键字", required = true, example = "product")
    @NotBlank(message = "请输入要查询的表名称")
    private String tableName;

    /**
     * 查询字段
     */
    @ApiModelProperty(value = "查询字段", required = true, example = "[goodsNo]")
    @NotEmpty(message = "请输入要查询的字段")
    private String[] fields;
}
