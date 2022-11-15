package com.aircraftcarrier.marketing.store.client.demo.request;

import com.aircraftcarrier.framework.support.validation.InEnum;
import com.aircraftcarrier.framework.tookit.DateTimeUtil;
import com.aircraftcarrier.framework.web.config.SerializerConfiguration;
import com.aircraftcarrier.framework.web.ser.Date2ShortStringSerializer;
import com.aircraftcarrier.marketing.store.common.enums.DemoEnum;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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

    @JSONField(format = DateTimeUtil.STANDARD_FORMAT)
    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    private Date time;

    @JsonSerialize(using = Date2ShortStringSerializer.class)
    private Date time2;

    /**
     * 全局默认
     * {@link SerializerConfiguration#jsonCustomizer() }
     * {@link com.aircraftcarrier.marketing.store.adapter.config.WebMvcConfiguration#extendMessageConverters(java.util.List) }
     */
    private Date time3;
}
