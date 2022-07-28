package com.aircraftcarrier.framework.tookit;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * MoneyUtilTest
 * 可封装 {@link cn.hutool.core.util.NumberUtil} 相关方法。
 * 涉及金额相关工具类的方法封装必须提供对应方法的各种情况的测试用例！
 * 对于金额类：高效安全 > 安全 > 高效
 *
 * @author zhipengliu
 * @date 2022/7/28
 * @since 1.0
 */
@Slf4j
public class MoneyUtilTest {

    @Test
    public void testMoneyUtil3() {
        BigDecimal bigDecimal = BigDecimal.valueOf(999.9);
        System.out.println(bigDecimal.multiply(bigDecimal));

        // 一分钱也是挺重要的！！！整数越大误差越大
        BigDecimal bigDecimal2 = BigDecimal.valueOf(999.8);
        System.out.println(bigDecimal2.multiply(bigDecimal2));
    }

    @Test
    public void testMoneyUtil2() {
        // 注意不要new BigDecimal
        BigDecimal bigDecimal = new BigDecimal(0.02);
        System.out.println(bigDecimal);

        BigDecimal bigDecimal1 = BigDecimal.valueOf(0.02);
        System.out.println(bigDecimal1);
    }

    @Test
    public void testMoneyUtil1() {
        long start = LogTimeUtil.startTime();

        BigDecimal moneyB = null;
        for (int i = 0; i < 10000; i++) {
            BigDecimal bigDecimal = BigDecimal.valueOf(i);
            BigDecimal multiply = bigDecimal.multiply(BigDecimal.valueOf(i + 1));
            moneyB = multiply.divide(BigDecimal.valueOf(i + 1), RoundingMode.HALF_DOWN);
        }
        log.info(JSON.toJSONString(moneyB));

        log.info("耗时: {}", LogTimeUtil.endTime(start));
    }

    @Test
    public void testMoneyUtilDivide() {
        Stopwatch stopwatch = LogTimeUtil.startStopwatchTime();

        Money money = null;
        for (int i = 0; i < 10000; i++) {
            money = MoneyUtil.divide(i + 1, i);
            money = money.divide(i);
        }
        log.info(JSON.toJSONString(money));

        log.info("耗时: {}", LogTimeUtil.endStopwatchTime(stopwatch));
    }

    @Test
    public void testMoneyUtilMultiply() {
        Stopwatch stopwatch = LogTimeUtil.startStopwatchTime();

        Money money = null;
        for (int i = 0; i < 10000; i++) {
            money = MoneyUtil.multiply(i, i + 1);
            money = money.multiply(i + 1);
        }
        log.info(JSON.toJSONString(money));

        log.info("耗时: {}", LogTimeUtil.endStopwatchTime(stopwatch));
    }

    @Test
    public void testMoneyUtilSubtract() {
        Stopwatch stopwatch = LogTimeUtil.startStopwatchTime();

        Money money = null;
        for (int i = 0; i < 10000; i++) {
            money = MoneyUtil.subtract(i + 1, i);
            money = money.subtract(money);
        }
        log.info(JSON.toJSONString(money));

        log.info("耗时: {}", LogTimeUtil.endStopwatchTime(stopwatch));
    }

    @Test
    public void testMoneyUtilAdd() {
        Stopwatch stopwatch = LogTimeUtil.startStopwatchTime();

        Money money = null;
        for (int i = 0; i < 10000; i++) {
            money = MoneyUtil.add(i, i);
            money = money.add(money);
        }
        log.info(JSON.toJSONString(money));

        log.info("耗时: {}", LogTimeUtil.endStopwatchTime(stopwatch));
    }

    @Test
    public void testMoneyUtilYuanToCent() {
        long cent = MoneyUtil.yuanToCent(100.998);
        log.info("cent: {}", cent);
    }

    @Test
    public void testMoneyUtilCentToYuan() {
        long cent = MoneyUtil.yuanToCent(100.998);
        double yuan = MoneyUtil.centToYuan(cent);
        log.info("yuan: {}", yuan);
    }
}
