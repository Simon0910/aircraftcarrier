package com.aircraftcarrier.framework.concurrent.comletablefuture;

import lombok.Getter;
import lombok.Setter;

/**
 * 类注释内容
 *
 * @author zhipengliu
 * @date 2023/5/2
 * @since 1.0
 */
@Getter
@Setter
public class Order {
    long start = System.currentTimeMillis();
    String i;
}
