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


}
