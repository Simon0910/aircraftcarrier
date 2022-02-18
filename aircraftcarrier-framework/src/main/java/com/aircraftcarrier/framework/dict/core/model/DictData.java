package com.aircraftcarrier.framework.dict.core.model;

import com.aircraftcarrier.framework.enums.StatusEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * 字典数据 Response DTO
 *
 * @author yudao
 */
@Getter
@Setter
public class DictData {

    /**
     * 字典标签
     */
    private String label;

    /**
     * 字典值
     */
    private String value;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 状态
     * <p>
     * 枚举 {@link StatusEnum }
     */
    private Integer status;
}
