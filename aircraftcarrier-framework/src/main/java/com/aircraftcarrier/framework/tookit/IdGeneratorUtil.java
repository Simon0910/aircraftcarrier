package com.aircraftcarrier.framework.tookit;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

/**
 * @author lzp
 */
public class IdGeneratorUtil {
    /**
     * IdGeneratorUtil
     */
    private IdGeneratorUtil() {
    }

    /**
     * 生成唯一主键 16位 纯数字
     *
     * @return
     */
    public static Long generatorId() {
        Snowflake snowflake = IdUtil.getSnowflake(1, 1);
        return snowflake.nextId();
    }


    /**
     * 生成唯一主键 32位 小写字母和数字
     *
     * @return
     */
    public static String fastSimpleUuid() {
        return IdUtil.fastSimpleUUID();
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
