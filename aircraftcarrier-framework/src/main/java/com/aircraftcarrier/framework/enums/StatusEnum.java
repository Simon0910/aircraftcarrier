package com.aircraftcarrier.framework.enums;

import com.aircraftcarrier.framework.tookit.MapUtil;
import lombok.Getter;

import java.util.HashMap;

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
    private static final HashMap<Integer, StatusEnum> MAPPINGS = MapUtil.newHashMap(values().length);

    static {
        for (StatusEnum value : values()) {
            MAPPINGS.put(value.getCode(), value);
        }
    }

    private final Integer code;
    private final String name;

    StatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
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

    @Override
    public Integer code() {
        return code;
    }

    @Override
    public String desc() {
        return name;
    }

}
