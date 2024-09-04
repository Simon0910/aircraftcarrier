package com.aircraftcarrier.framework.exceltask;

import cn.hutool.core.text.StrPool;

/**
 * @author liuzhipeng
 * @since 2024/9/4
 */
public class ExcelUtil {
    public static String getRowPosition(AbstractExcelRow t) {
        return t.getSheetNo() + StrPool.UNDERLINE + t.getRowNo();
    }

    public static int comparePosition(String s1, String s2) {
        String[] split1 = s1.split(StrPool.UNDERLINE);
        String[] split2 = s2.split(StrPool.UNDERLINE);
        int result = Integer.parseInt(split1[0]) - Integer.parseInt(split2[0]);
        if (result == 0) {
            result = Integer.parseInt(split1[1]) - Integer.parseInt(split2[1]);
        }
        return result;
    }
}
