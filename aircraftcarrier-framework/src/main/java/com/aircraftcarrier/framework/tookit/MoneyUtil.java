package com.aircraftcarrier.framework.tookit;

import org.javamoney.moneta.Money;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

/**
 * MoneyUtil
 *
 * @author zhipengliu
 * @date 2022/7/28
 * @since 1.0
 */
public class MoneyUtil {

    /**
     * 人民币
     */
    private static final CurrencyUnit CNY = Monetary.getCurrency("CNY");

    private MoneyUtil() {
    }

    /**
     * 加
     *
     * @param money1 money1
     * @param money2 money2
     * @return Money
     */
    public static Money add(Number money1, Number money2) {
        Money amount1 = Money.of(money1, CNY);
        Money amount2 = Money.of(money2, CNY);
        return amount1.add(amount2);
    }

    /**
     * 减
     *
     * @param money1 money1
     * @param money2 money2
     * @return Money
     */
    public static Money subtract(Number money1, Number money2) {
        Money amount1 = Money.of(money1, CNY);
        Money amount2 = Money.of(money2, CNY);
        return amount1.subtract(amount2);
    }

    /**
     * 乘
     *
     * @param money      money
     * @param multiplier multiplier
     * @return Money
     */
    public static Money multiply(Number money, Number multiplier) {
        Money amount1 = Money.of(money, CNY);
        return amount1.multiply(multiplier);
    }

    /**
     * 除
     *
     * @param money   money
     * @param divisor divisor
     * @return Money
     */
    public static Money divide(Number money, Number divisor) {
        Money amount1 = Money.of(money, CNY);
        return amount1.divide(divisor);
    }

    /**
     * 金额元转换为分
     *
     * @param yuan 金额，单位元
     * @return 金额，单位分
     * @since 5.7.11
     */
    public static long yuanToCent(double yuan) {
        return new cn.hutool.core.math.Money(yuan).getCent();
    }

    /**
     * 金额分转换为元
     *
     * @param cent 金额，单位分
     * @return 金额，单位元
     * @since 5.7.11
     */
    public static double centToYuan(long cent) {
        long yuan = cent / 100;
        int centPart = (int) (cent % 100);
        return new cn.hutool.core.math.Money(yuan, centPart).getAmount().doubleValue();
    }

}
