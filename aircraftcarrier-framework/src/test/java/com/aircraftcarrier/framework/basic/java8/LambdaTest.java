package com.aircraftcarrier.framework.basic.java8;

import org.junit.Test;

import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.List;

/**
 * https://www.wdbyte.com
 */
public class LambdaTest {

    /**
     * 数学计算测试
     */
    @Test
    public void mapMathTest() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        IntSummaryStatistics stats = list.stream().mapToInt(x -> x).summaryStatistics();
        System.out.println("最小值：" + stats.getMin());
        System.out.println("最大值：" + stats.getMax());
        System.out.println("个数：" + stats.getCount());
        System.out.println("和：" + stats.getSum());
        System.out.println("平均数：" + stats.getAverage());
        // 求和的另一种方式
        Integer integer = list.stream().reduce((sum, cost) -> sum + cost).get();
        System.out.println(integer);
    }


    @Test
    public void test() {
        List<String> skills = Arrays.asList("java", "golang", "c++", "c", "python", "java");
        // Jdk8 之后转字符串
        System.out.println(String.join(",", skills));
    }
}
