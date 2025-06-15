package com.aircraftcarrier.framework.enums;

import com.aircraftcarrier.framework.tookit.MapUtil;

import java.io.Serializable;
import java.util.Map;

/**
 * EnumUtil
 *
 * @author zhipengliu
 * @date 2025/6/15
 * @since 1.0
 */
public class EnumUtil {

    private EnumUtil() {
    }

    public static <E extends Enum<E> & IEnum<K>, K extends Serializable> Map<K, E> initMapping(Class<E> enumClass) {
        E[] enumConstants = enumClass.getEnumConstants();
        Map<K, E> mapping = MapUtil.newHashMap(enumConstants.length);
        for (E value : enumConstants) {
            mapping.put(value.getCode(), value);
        }
        return mapping;
    }


}
