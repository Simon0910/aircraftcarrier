//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.aircraftcarrier.framework.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * @author lzp
 */

@Getter
@AllArgsConstructor
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
    private static final Map<Integer, YnValueEnum> MAPPINGS = EnumUtil.initMapping(YnValueEnum.class);

    private final Integer code;
    private final String desc;


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
