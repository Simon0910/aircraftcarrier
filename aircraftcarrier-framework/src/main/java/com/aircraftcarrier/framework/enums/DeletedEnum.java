//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.aircraftcarrier.framework.enums;

import com.aircraftcarrier.framework.tookit.MapUtil;
import lombok.Getter;

import java.util.Map;

/**
 * @author lzp
 */
@Getter
public enum DeletedEnum implements IEnum<Integer> {
    /**
     * 未删除
     */
    NORMAL(0, "未删除"),
    /**
     * 已删除
     */
    DELETED(1, "已删除"),
    ;

    /**
     * MAPPINGS
     */
    private static final Map<Integer, DeletedEnum> MAPPINGS = MapUtil.newHashMap(values().length);

    static {
        for (DeletedEnum value : values()) {
            MAPPINGS.put(value.getCode(), value);
        }
    }

    private final Integer code;
    private final String desc;

    DeletedEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * code => 枚举
     */
    public static DeletedEnum resolve(Integer code) {
        return MAPPINGS.get(code);
    }

    public static DeletedEnum convertCode(Integer code) {
        return MAPPINGS.get(code);
    }

}
