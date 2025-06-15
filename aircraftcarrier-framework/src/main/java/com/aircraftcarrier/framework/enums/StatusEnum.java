package com.aircraftcarrier.framework.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * @author lzp
 */

@Getter
@AllArgsConstructor
public enum StatusEnum implements IEnum<Integer> {
    /**
     * 开启
     */
    ENABLE(1, "开启"),
    /**
     * 关闭
     */
    DISABLE(0, "关闭");

    /**
     * MAPPINGS
     */
    private static final Map<Integer, StatusEnum> MAPPINGS = EnumUtil.initMapping(StatusEnum.class);

    private final Integer code;
    private final String desc;

    /**
     * code => 枚举
     */
    public static StatusEnum resolve(Integer code) {
        return MAPPINGS.get(code);
    }

    public static StatusEnum convertCode(Integer code) {
        return MAPPINGS.get(code);
    }

}
