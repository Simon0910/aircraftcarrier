package com.aircraftcarrier.framework.enums;

import com.aircraftcarrier.framework.tookit.MapUtil;
import lombok.Getter;

import java.util.Map;

/**
 * @author lzp
 */
@Getter
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
    private static final Map<Integer, StatusEnum> MAPPINGS = MapUtil.newHashMap(values().length);

    static {
        for (StatusEnum value : values()) {
            MAPPINGS.put(value.getCode(), value);
        }
    }

    private final Integer code;
    private final String desc;

    StatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

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
