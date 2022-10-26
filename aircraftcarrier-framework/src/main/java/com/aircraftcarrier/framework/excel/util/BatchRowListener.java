package com.aircraftcarrier.framework.excel.util;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 读取excel 多行批次处理 Listener
 *
 * @author lishudong
 * @version 1.0
 * @date 2018/11/8
 */
public interface BatchRowListener<T extends ExcelRow> {

    Logger logger = LoggerFactory.getLogger(SingleRowListener.class);

    /**
     * invokeHeadMap
     *
     * @param headMap headMap
     * @param context context
     * @return boolean
     */
    default boolean invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        return false;
    }

    /**
     * 多行批次处理
     *
     * @param rowList         每个批次对应的多行数据
     * @param analysisContext analysisContext
     */
    void batchInvoke(List<T> rowList, AnalysisContext analysisContext);

    /**
     * doAfterAllAnalysed
     *
     * @param analysisContext analysisContext
     */
    default void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    /**
     * onException
     *
     * @param exception       exception
     * @param analysisContext analysisContext
     * @throws Exception
     */
    default void onException(Exception exception, AnalysisContext analysisContext) throws Exception {
        if (exception instanceof ExcelDataConvertException excelDataConvertException) {
            logger.error("第{}行，第{}列解析异常", excelDataConvertException.getRowIndex(),
                    excelDataConvertException.getColumnIndex(), excelDataConvertException);
        } else {
            throw exception;
        }
    }
}
