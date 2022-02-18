package com.aircraftcarrier.marketing.store.client.demo.view;

import com.aircraftcarrier.marketing.store.common.enums.DataTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lzp
 */
@Data
@ApiModel(value = "DemoPageVo")
public class DemoPageVo implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    @ApiModelProperty(value = "主键", required = true, example = "1")
    private Long id;

    /**
     * 业务主键
     */
    @ApiModelProperty(value = "业务主键", required = true, example = "20201314")
    private String bizNo;

    /**
     * 商家编码
     */
    @ApiModelProperty(value = "商家编码", required = true, example = "ECP2020")
    private String sellerNo;

    /**
     * 商家名称
     */
    @ApiModelProperty(value = "商家名称", required = true, example = "京城国际")
    private String sellerName;

    /**
     * 枚举演示
     */
    @ApiModelProperty(value = "枚举演示", required = true, example = "NORMAL")
    private DataTypeEnum dataType;
}
