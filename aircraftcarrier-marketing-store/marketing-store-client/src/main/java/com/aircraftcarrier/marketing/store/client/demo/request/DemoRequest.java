package com.aircraftcarrier.marketing.store.client.demo.request;

import com.aircraftcarrier.framework.support.validation.InEnum;
import com.aircraftcarrier.framework.tookit.DateTimeUtil;
import com.aircraftcarrier.framework.web.config.SerializerConfiguration;
import com.aircraftcarrier.framework.web.deser.Timestamp2DateDeserializer;
import com.aircraftcarrier.framework.web.ser.Date2ShortStringSerializer;
import com.aircraftcarrier.marketing.store.common.enums.DemoEnum;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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

    /**
     * 可接收：
     * "2022-11-16 15:55:02"
     * "2022-11-16"
     * 返回：
     * 2022-11
     */
    @JSONField(format = DateTimeUtil.STANDARD_FORMAT)
    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    private Date time;

    /**
     * 可接收：
     * {@link Timestamp2DateDeserializer} 时间戳 1668585302000
     * 返回：
     * {@link Date2ShortStringSerializer} yyyy-MM-dd
     */
    @JsonSerialize(using = Date2ShortStringSerializer.class)
    @JsonDeserialize(using = Timestamp2DateDeserializer.class)
    private Date time2;

    /**
     * 全局默认：
     * 接受：
     * "2022-11-16 15:55:02"
     * 返回：
     * "2022-11-16 15:55:02"
     * <p>
     * 此配置方式会改变源码原有逻辑 {@link SerializerConfiguration#jsonCustomizer() } 相当于重写顶层默认逻辑，导致@JsonFormat失效
     * 以下方式只替换部分实现，不改变程序执行逻辑，不影响@JsonFormat，方法为spring提供的扩展入口
     * {@link com.aircraftcarrier.marketing.store.adapter.config.WebMvcConfiguration#extendMessageConverters(java.util.List) }
     */
    private Date time3;
}
