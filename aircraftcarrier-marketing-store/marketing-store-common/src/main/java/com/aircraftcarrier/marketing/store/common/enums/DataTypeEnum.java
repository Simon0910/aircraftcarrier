package com.aircraftcarrier.marketing.store.common.enums;

import com.aircraftcarrier.framework.enums.EnumUtil;
import com.aircraftcarrier.framework.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * @author lzp
 */
@Getter
@AllArgsConstructor
public enum DataTypeEnum implements IEnum<Integer> {
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
    private static final Map<Integer, DataTypeEnum> MAPPINGS = EnumUtil.initMapping(DataTypeEnum.class);

    /**
     * code
     */
    private final Integer code;
    /**
     * desc
     */
    private final String desc;

    /**
     * code => 枚举
     */
    public static DataTypeEnum resolve(Integer code) {
        return MAPPINGS.get(code);
    }

    public static DataTypeEnum convertDesc(String desc) {
        for (DataTypeEnum iEnum : MAPPINGS.values()) {
            if (iEnum.getDesc().equals(desc)) {
                return iEnum;
            }
        }
        return null;
    }


}
