package com.aircraftcarrier.framework.exceltask;

import cn.hutool.core.text.StrPool;

/**
 * @author liuzhipeng
 * @since 2024/9/4
 */
public class ExcelUtil {
    public static String getRowPosition(AbstractExcelRow row) {
        return row.getSheetNo() + StrPool.UNDERLINE + row.getRowNo();
    }

    public static int comparePosition(String sheetNoRowNo1, String sheetNoRowNo2) {
        String[] split1 = sheetNoRowNo1.split(StrPool.UNDERLINE);
        String[] split2 = sheetNoRowNo2.split(StrPool.UNDERLINE);
        int result = Integer.parseInt(split1[0]) - Integer.parseInt(split2[0]);
        if (result == 0) {
            result = Integer.parseInt(split1[1]) - Integer.parseInt(split2[1]);
        }
        return result;
    }
}
