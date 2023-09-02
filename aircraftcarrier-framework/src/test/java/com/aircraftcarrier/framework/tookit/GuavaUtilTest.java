package com.aircraftcarrier.framework.tookit;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 类注释内容
 *
 * @author zhipengliu
 * @date 2023/9/3
 * @since 1.0
 */
public class GuavaUtilTest {

    public static void main(String[] args) {
        List<Integer> numbers = Lists.newArrayList(1, 3, 8, 2, 6, 4, 7, 5, 9, 0);
        System.out.println(GuavaUtil.topN(numbers, 3));
        System.out.println(GuavaUtil.bottomN(numbers, 3));
    }
}
