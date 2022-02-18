package com.aircraftcarrier.marketing.store.client.demo.request;

import com.aircraftcarrier.framework.model.request.AbstractRequest;
import com.aircraftcarrier.marketing.store.common.enums.DataTypeEnum;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author lzp
 */
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
    private DataTypeEnum dataType;
}
