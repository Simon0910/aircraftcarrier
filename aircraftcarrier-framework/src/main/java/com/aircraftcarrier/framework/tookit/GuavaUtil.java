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

    public static void main(String[] args) {
        List<Integer> numbers = Lists.newArrayList(1, 3, 8, 2, 6, 4, 7, 5, 9, 0);
        System.out.println(topN(numbers, 3));
        System.out.println(bottomN(numbers, 3));


        String track = "init,split,prexml,singleSetSendpayModify,pre4Galileo,transferMarking,transfer,activate,deliveryInstall,route,sendpayModify,preMarking,sorting,marking,bigApp,activate,transferWorker,riskControl,orderEvent,print,offline,testerMarketing,globalMarking,collData,preDownFlag,down,poc,finish";

        Iterator<String> iterator = Splitter.on(",").split(track).iterator();
        List<String> trackNodeList = new ArrayList<>();
        while (iterator.hasNext()) {
            String nodeName = iterator.next();
            trackNodeList.add(nodeName);
        }

        Map<String, String> tempMap = new LinkedHashMap<>(trackNodeList.size() * 2);

        for (int i = 0; i < trackNodeList.size(); i++) {
            tempMap.put(trackNodeList.get(i), trackNodeList.get(i));
        }
        ArrayList<String> strings = new ArrayList<>(1000);
        strings.addAll(tempMap.values());

        String track2 = "init,split,prexml,singleSetSendpayModify,pre4Galileo,transferMarking,transfer,activate,deliveryInstall,route,sendpayModify,preMarking,sorting,marking,bigApp,activate,transferWorker,riskControl,orderEvent,print,offline,testerMarketing,globalMarking,collData,preDownFlag,down,poc,finish";

        Iterator<String> iterator2 = Splitter.on(",").split(track2).iterator();
        List<String> trackNodeList2 = new ArrayList<>();
        while (iterator2.hasNext()) {
            String nodeName = iterator2.next();
            trackNodeList2.add(nodeName);
        }

        for (int i = 0; i < trackNodeList2.size(); i++) {
            strings.add(i, trackNodeList.get(i) + "##");
        }

        System.out.println(strings);

    }


}
