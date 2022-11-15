package com.aircraftcarrier.marketing.store.client.demo.request;

import com.aircraftcarrier.framework.support.validation.InEnum;
import com.aircraftcarrier.marketing.store.common.enums.DemoEnum;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

/**
 * @author lzp
 */
@Data
@ApiModel(value = "DemoRequest")
public class DemoRequest {

    @InEnum(DemoEnum.class)
    private Integer demoEnum;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
//    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date time;

    private Date time2;
}
