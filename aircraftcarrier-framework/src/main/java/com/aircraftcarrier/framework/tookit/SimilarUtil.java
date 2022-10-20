package com.aircraftcarrier.framework.tookit;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 * @author liuzhipeng
 */
public class SimilarUtil {

    /**
     * 计算两个字符串的相似度
     *
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 相似度
     * @since 3.2.3
     */
    public static double similar(String str1, String str2) {
        return StrUtil.similar(str1, str2);
    }

    public static Integer similarLevenshtein(String str1, String str2) {
        return LevenshteinDistance.getDefaultInstance().apply(str1, str2);
    }


    public static void main(String[] args) {
        long l = LogTimeUtil.startTime();
        double d = similar("福建省", "234福232建32省2");
        System.out.println(LogTimeUtil.endTimeStr(l));

        l = LogTimeUtil.startTime();
        Integer i = similarLevenshtein("福建省", "234福232建32省2");
        System.out.println(LogTimeUtil.endTimeStr(l));

        System.out.println(d + " :: " + i);
        System.out.println(similar("福建省", "福建城") + " :: " + similarLevenshtein("福建省", "福建城"));
        System.out.println(similar("福建省", "湖建省") + " :: " + similarLevenshtein("福建省", "湖建省"));
    }


}
