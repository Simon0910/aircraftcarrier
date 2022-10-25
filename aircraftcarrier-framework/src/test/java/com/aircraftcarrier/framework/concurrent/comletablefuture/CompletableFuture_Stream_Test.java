package com.aircraftcarrier.framework.concurrent.comletablefuture;

import com.aircraftcarrier.framework.tookit.SleepUtil;
import com.aircraftcarrier.framework.tookit.TimeLogUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Slf4j
public class CompletableFuture_Stream_Test {

    /**
     * 正确的方式
     * https://mp.weixin.qq.com/s/BX-EvTOMWc8d4H0mK6wpSA*
     */
    @Test
    public void comparePriceInOnePlat2() {
        List<String> products = List.of("JD", "TB", "PDD");
        long l = TimeLogUtil.beginTime();
        Integer integer = products.stream()
                .map(product ->
                        CompletableFuture.supplyAsync(() -> getPrice(product))
                                .thenCombine(
                                        CompletableFuture.supplyAsync(() -> getDiscounts(product)), this::computeRealPrice)
                )
                .collect(Collectors.toList()).stream()
                .map(CompletableFuture::join).min(Comparator.comparingInt(Integer::intValue))
                .orElse(-1);
        TimeLogUtil.endTimeLog("comparePriceInOnePlat2: {}", l);
    }

    /**
     * 串行
     */
    @Test
    public void comparePriceInOnePlat() {
        List<String> products = List.of("JD", "TB", "PDD");
        long l = TimeLogUtil.beginTime();
        products.stream()
                .map(product ->
                        CompletableFuture.supplyAsync(() -> getPrice(product))
                                .thenCombine(
                                        CompletableFuture.supplyAsync(() -> getDiscounts(product)), this::computeRealPrice)
                )
                .map(CompletableFuture::join).min(Comparator.comparingInt(Integer::intValue))
                .orElse(-1);
        TimeLogUtil.endTimePrintln(l);

        // 等价于==>

        l = TimeLogUtil.beginTime();
        products.stream()
                .map(product ->
                        CompletableFuture.supplyAsync(() -> getPrice(product))
                                .thenCombine(
                                        CompletableFuture.supplyAsync(() -> getDiscounts(product)), this::computeRealPrice)
                                .join()
                )
                .min(Comparator.comparingInt(Integer::intValue))
                .orElse(-1);
        TimeLogUtil.endTimePrintln("comparePriceInOnePlat: {}", l);
    }


    private Integer getPrice(String product) {
        // 淘宝
        // 京东
        // 多多
        log.info("获取价格 {}", product);
        SleepUtil.sleepSeconds(1);
        return 5000;
    }

    private Integer getDiscounts(String product) {
        // 淘宝
        // 京东
        // 多多
        log.info("获取折扣 {}", product);
        SleepUtil.sleepSeconds(1);
        return 5000;
    }

    private Integer computeRealPrice(Integer o, Integer product) {
        // 淘宝
        // 京东
        // 多多
        log.info("计算： {}", product);
        return 5000;
    }

    private Integer HttpRequestMock(Integer price) {
        return 5000;
    }

}
