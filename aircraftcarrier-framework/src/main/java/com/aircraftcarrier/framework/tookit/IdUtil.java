package com.aircraftcarrier.framework.tookit;

import cn.hutool.core.lang.Snowflake;

/**
 * @author lzp
 */
public class IdUtil {
    /**
     * IdGeneratorUtil
     */
    private IdUtil() {
    }

    /**
     * 生成唯一主键 16位 纯数字
     *
     * @return
     */
    public static Long generatorId() {
        Snowflake snowflake = cn.hutool.core.util.IdUtil.getSnowflake(1, 1);
        return snowflake.nextId();
    }


    /**
     * 生成唯一主键 32位 小写字母和数字
     *
     * @return
     */
    public static String fastSimpleUuid() {
        return cn.hutool.core.util.IdUtil.fastSimpleUUID();
    }

    /**
     * 生成唯一主键
     *
     * @return
     */
    public static String generatorIdStr() {
        return String.valueOf(generatorId());
    }

    /**
     * 生成唯一主键
     *
     * @return
     */
    public static String generatorId(String prefix) {
        return prefix + generatorIdStr();
    }
}
