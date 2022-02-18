package com.aircraftcarrier.marketing.store.infrastructure.repository.dataobject;

import com.aircraftcarrier.framework.data.BaseDO;
import com.aircraftcarrier.marketing.store.common.enums.DataTypeEnum;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 审批流配置 Do
 *
 * @author lzp
 * @version 1.0
 * @date 2020-11-11
 */
@Data
@TableName(value = "`demo`")
public class DemoDo extends BaseDO<DemoDo> {

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
