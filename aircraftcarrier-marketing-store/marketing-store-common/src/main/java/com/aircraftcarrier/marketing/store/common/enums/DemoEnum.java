package com.aircraftcarrier.marketing.store.common.enums;

import com.aircraftcarrier.framework.enums.IEnum;
import com.aircraftcarrier.framework.tookit.MapUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * @author lzp
 */

@Getter
@AllArgsConstructor
public enum DemoEnum implements IEnum<Integer> {
    /**
     * 正常
     */
    NORMAL(1, "正常"),
    /**
     * 普通
     */
    GENERAL(2, "普通"),
    /**
     * 超级
     */
    SUPPER(3, "超级");

    /**
     * MAPPINGS
     */
    private static final Map<Integer, DemoEnum> MAPPINGS = MapUtil.newHashMap(values().length);

    static {
        for (DemoEnum value : values()) {
            MAPPINGS.put(value.getCode(), value);
        }
    }

    /**
     * code
     */
    private final Integer code;
    /**
     * desc
     */
    private final String desc;

}
