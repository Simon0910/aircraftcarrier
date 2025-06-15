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
public enum YnValueEnum implements IEnum<Integer> {
    /**
     * 有效
     */
    Y(1, "有效"),
    /**
     * 无效
     */
    N(0, "无效"),
    ;

    /**
     * MAPPINGS
     */
    private static final Map<Integer, YnValueEnum> MAPPINGS = MapUtil.newHashMap(values().length);

    static {
        for (YnValueEnum value : values()) {
            MAPPINGS.put(value.getCode(), value);
        }
    }

    private final Integer code;
    private final String desc;

    YnValueEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static Integer yesCode() {
        return Y.getCode();
    }

    public static Integer noCode() {
        return N.getCode();
    }

    /**
     * code => 枚举
     */
    public static YnValueEnum resolve(Integer code) {
        return MAPPINGS.get(code);
    }

    public static YnValueEnum convertCode(Integer code) {
        return MAPPINGS.get(code);
    }

}
