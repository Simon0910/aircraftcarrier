package com.aircraftcarrier.marketing.store.adapter.common;

import com.aircraftcarrier.framework.excel.handler.CommentRowWriteHandler;
import com.aircraftcarrier.framework.excel.handler.DropDownSheetWriteHandler;
import com.aircraftcarrier.framework.excel.strategy.StyleStrategy;
import com.aircraftcarrier.framework.excel.util.EasyExcelWriteUtil;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
                                   List<T> sourceList, Class<T> targetClass) throws IOException {
        EasyExcelWriteUtil.exportExcel(response, fileName, sheetName, sourceList, targetClass,
                new LongestMatchColumnWidthStyleStrategy(),
                StyleStrategy.customHorizontalCellStyleStrategy(),
                new DropDownSheetWriteHandler(targetClass),
                new CommentRowWriteHandler(targetClass));
    }


    protected <S, T> void exportExcelToTarget(HttpServletResponse response, String fileName, String sheetName,
                                              List<S> sourceList, Class<T> targetClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        EasyExcelWriteUtil.exportExcelToTarget(response, fileName, sheetName, sourceList, targetClass,
                new LongestMatchColumnWidthStyleStrategy(),
                StyleStrategy.customHorizontalCellStyleStrategy(),
                new DropDownSheetWriteHandler(targetClass),
                new CommentRowWriteHandler(targetClass));
    }
}
