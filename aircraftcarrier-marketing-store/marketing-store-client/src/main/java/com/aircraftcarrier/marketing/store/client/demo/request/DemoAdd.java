package com.aircraftcarrier.marketing.store.client.demo.request;

import com.aircraftcarrier.framework.model.request.AbstractRequest;
import com.aircraftcarrier.marketing.store.common.enums.DataTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lzp
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "DemoAdd")
public class DemoAdd extends AbstractRequest {
    /**
     * 业务主键
     */
    private String bizNo;

    /**
     * 商家编码
     */
    private String sellerNo;

    /**
     * 商家名称
     */
    private String sellerName;

    /**
     * 说明
     */
    private String description;

    /**
     * 枚举演示
     */
    @ApiModelProperty(value = "枚举演示", required = true, example = "NORMAL")
    private DataTypeEnum dataType;
}
