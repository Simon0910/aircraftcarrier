package com.aircraftcarrier.framework.tookit;

/**
 * @author lzp
 */
public class RandomUtil {

    private RandomUtil() {
    }

    public static int nextInt(int max) {
        return cn.hutool.core.util.RandomUtil.randomInt(max);
    }

    public static int nextInt(int min, int max) {
        return cn.hutool.core.util.RandomUtil.randomInt(min, max);
    }

}
