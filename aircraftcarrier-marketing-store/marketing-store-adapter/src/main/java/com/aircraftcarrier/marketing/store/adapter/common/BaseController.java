package com.aircraftcarrier.marketing.store.adapter.common;

import com.aircraftcarrier.framework.excel.handler.CommentRowWriteHandler;
import com.aircraftcarrier.framework.excel.handler.DropDownSheetWriteHandler;
import com.aircraftcarrier.framework.excel.strategy.StyleStrategy;
import com.aircraftcarrier.framework.excel.util.EasyExcelWriteUtil;
import com.aircraftcarrier.framework.security.core.LoginUserUtil;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

/**
 * @author lzp
 */
public class BaseController {

    public String getOperator(HttpServletRequest request) {
        return Objects.requireNonNull(LoginUserUtil.getLoginUserId()).toString();
    }

    protected <T> void exportExcel(HttpServletResponse response, String fileName, String sheetName,
                                   List<?> sourceList, Class<T> modelClass) throws Exception {
        EasyExcelWriteUtil.exportExcelToTarget(response, fileName, sheetName, sourceList, modelClass,
                new LongestMatchColumnWidthStyleStrategy(),
                StyleStrategy.customHorizontalCellStyleStrategy(),
                new DropDownSheetWriteHandler(modelClass),
                new CommentRowWriteHandler(modelClass));
    }
}
