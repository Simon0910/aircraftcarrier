package com.aircraftcarrier.marketing.store.domain.model.demo;

import com.aircraftcarrier.framework.core.Entity;
import com.aircraftcarrier.framework.model.AbstractDTO;
import com.aircraftcarrier.marketing.store.common.enums.DataTypeEnum;
import lombok.Data;

/**
 * @author lzp
 */
@Entity
@Data
public class DemoEntity extends AbstractDTO<DemoEntity> {
    /**
     * 主键
     */
    private Long id;

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
     * 枚举演示
     */
    private DataTypeEnum dataType;

}
