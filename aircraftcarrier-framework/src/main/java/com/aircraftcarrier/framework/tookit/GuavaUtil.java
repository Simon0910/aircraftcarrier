package com.aircraftcarrier.framework.tookit;

import com.google.common.base.Splitter;
import com.google.common.collect.Comparators;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

/**
 * GuavaUtil
 *
 * @author liuzhipeng
 */
public class GuavaUtil {

    public static <T extends Comparable<? super T>> List<T> topN(List<T> list, int n) {
        Collector<T, ?, List<T>> greatest = Comparators.greatest(n, Comparator.naturalOrder());
        return list.stream().collect(greatest);
    }

    public static <T extends Comparable<? super T>> List<T> bottomN(List<T> list, int n) {
        Collector<T, ?, List<T>> greatest = Comparators.least(n, Comparator.naturalOrder());
        return list.stream().collect(greatest);
    }


}
