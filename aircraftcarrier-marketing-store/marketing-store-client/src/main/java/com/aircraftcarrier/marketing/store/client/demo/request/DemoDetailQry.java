package com.aircraftcarrier.marketing.store.client.demo.request;

import com.aircraftcarrier.framework.model.request.Query;
import com.aircraftcarrier.marketing.store.common.enums.DataTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lzp
 */
@Data
@ApiModel(value = "DemoDetailQry")
public class DemoDetailQry extends Query {
    /**
     * 主键
     */
    @ApiModelProperty(value = "主键", example = "1")
    private Long id;

    /**
     * 业务主键
     */
    @ApiModelProperty(value = "业务主键", example = "20200627161601001")
    private String bizNo;

    /**
     * 商家编码
     */
    @ApiModelProperty(value = "商家编码", example = "ECP20200627001")
    private String sellerNo;

    /**
     * 商家名称
     */
    @ApiModelProperty(value = "商家名称", example = "北京物流集团")
    private String sellerName;

    /**
     * 枚举演示
     */
    @ApiModelProperty(value = "枚举演示", required = true, example = "NORMAL")
    private DataTypeEnum dataType;
}
