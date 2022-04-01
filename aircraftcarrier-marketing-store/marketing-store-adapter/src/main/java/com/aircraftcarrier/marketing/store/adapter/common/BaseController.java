package com.aircraftcarrier.marketing.store.adapter.common;

import com.aircraftcarrier.framework.excel.handler.CommentRowWriteHandler;
import com.aircraftcarrier.framework.excel.handler.DropDownSheetWriteHandler;
import com.aircraftcarrier.framework.excel.strategy.StyleStrategy;
import com.aircraftcarrier.framework.excel.util.EasyExcelWriteUtil;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author lzp
 */
public class BaseController {

    public String getOperator(HttpServletRequest request) {
        // 建议拦截器把LoginUserInfo放进缓存 (token: LoginUserInfo)
        // 并提供统一访问api
        return "pin";
    }

    protected <T> void exportExcel(HttpServletResponse response, String fileName, String sheetName,
                                   List<?> sourceList, Class<T> targetClass) throws Exception {
        EasyExcelWriteUtil.exportExcelToTarget(response, fileName, sheetName, sourceList, targetClass,
                new LongestMatchColumnWidthStyleStrategy(),
                StyleStrategy.customHorizontalCellStyleStrategy(),
                new DropDownSheetWriteHandler(targetClass),
                new CommentRowWriteHandler(targetClass));
    }
}
