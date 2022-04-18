package com.aircraftcarrier.marketing.store.adapter.common;

import com.aircraftcarrier.framework.excel.util.EasyExcelWriteUtil;
import com.aircraftcarrier.framework.security.core.LoginUserUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

/**
 * 基础 Controller
 *
 * @author lzp
 */
public class BaseController {

    public String getOperator(HttpServletRequest request) {
        return Objects.requireNonNull(LoginUserUtil.getLoginUserId()).toString();
    }

    protected <T> void exportExcel(HttpServletResponse response, String fileName, String sheetName,
                                   List<?> sourceList, Class<T> modelClass) throws Exception {
        EasyExcelWriteUtil.exportExcel(response, fileName, sheetName, sourceList, modelClass);
    }
}
