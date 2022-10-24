package com.aircraftcarrier.framework.tookit;

import java.text.NumberFormat;

/**
 * @author lzp
 */
public class MathUtil {

    public static double getPercentage(int molecular, int denominator) {
        return getPercentage(molecular, denominator, 2);
    }

    /**
     * 功能描述: 两个数计算百分比
     *
     * @param molecular   分子
     * @param denominator 分母
     * @return 百分比
     */
    public static double getPercentage(double molecular, double denominator) {
        return getPercentage(molecular, denominator, 2);
    }

    /**
     * 功能描述: 两个数计算百分比
     *
     * @param molecular   分子
     * @param denominator 分母
     * @param scale       范围
     * @return 百分比
     */
    public static double getPercentage(double molecular, double denominator, Integer scale) {
        if (molecular == 0 || denominator == 0) {
            return 0;
        }
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(scale);
        String result = numberFormat.format((molecular / denominator));
        return Double.parseDouble(result);
    }

}
