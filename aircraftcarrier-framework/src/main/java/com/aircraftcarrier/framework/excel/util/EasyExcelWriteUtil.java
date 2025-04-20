package com.aircraftcarrier.framework.excel.util;

import cn.hutool.core.collection.CollUtil;
import com.aircraftcarrier.framework.excel.convert.BigDecimalConvert;
import com.aircraftcarrier.framework.excel.handler.CommentRowWriteHandler;
import com.aircraftcarrier.framework.excel.handler.DropDownSheetWriteHandler;
import com.aircraftcarrier.framework.excel.strategy.StyleStrategy;
import com.aircraftcarrier.framework.tookit.StringUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.util.DateUtils;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import org.springframework.beans.BeanUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * excel工具类
 *
 * @author Mark sunlightcs@gmail.com
 */
public class EasyExcelWriteUtil {

    private EasyExcelWriteUtil() {
    }

    /**
     * Excel导出
     *
     * @param response      response
     * @param fileName      文件名
     * @param sheetName     sheetName
     * @param list          数据List
     * @param modelClass    对象Class
     * @param writeHandlers writeHandlers
     */
    public static <T> void exportExcel(HttpServletResponse response, String fileName, String sheetName, List<?> list, Class<T> modelClass, WriteHandler... writeHandlers) throws IOException {
        if (StringUtil.isBlank(fileName)) {
            //当前日期
            fileName = DateUtils.format(new Date(), DateUtils.DATE_FORMAT_10);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("UTF-8");
        fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.displayName());
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        ExcelWriterBuilder excelWriterBuilder = EasyExcelFactory.write(response.getOutputStream(), modelClass);

        // write excel convert
        excelWriterBuilder.registerConverter(new BigDecimalConvert());

        if (writeHandlers != null) {
            for (WriteHandler writeHandler : writeHandlers) {
                excelWriterBuilder.registerWriteHandler(writeHandler);
            }
        }
        excelWriterBuilder.sheet(sheetName).doWrite(list);
    }

    /**
     * Excel导出，先sourceList转换成List<targetClass>，再导出
     *
     * @param response   response
     * @param fileName   文件名
     * @param sheetName  sheetName
     * @param sourceList 原数据List
     * @param modelClass 目标对象Class
     */
    public static <T> void exportExcelToTarget(HttpServletResponse response, String fileName, String sheetName, List<?> sourceList,
                                               Class<T> modelClass, WriteHandler... writeHandlers) throws NoSuchMethodException, IOException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (CollUtil.isEmpty(sourceList)) {
            sourceList = new ArrayList<>();
        }

        if (!sourceList.isEmpty() && sourceList.get(0).getClass() != modelClass) {
            Constructor<T> constructor = modelClass.getDeclaredConstructor();
            List<T> targetList = new ArrayList<>(sourceList.size());
            for (Object source : sourceList) {
                T target = constructor.newInstance();
                BeanUtils.copyProperties(source, target);
                targetList.add(target);
            }
            exportExcel(response, fileName, sheetName, targetList, modelClass, writeHandlers);
        } else {
            exportExcel(response, fileName, sheetName, sourceList, modelClass, writeHandlers);
        }

    }


    /**
     * 统一格式导出
     *
     * @param response
     * @param fileName
     * @param sheetName
     * @param sourceList
     * @param modelClass
     * @param <T>
     * @throws Exception
     */
    public static <T> void exportExcel(HttpServletResponse response, String fileName, String sheetName,
                                       List<?> sourceList, Class<T> modelClass) throws Exception {
        exportExcelToTarget(response, fileName, sheetName, sourceList, modelClass,
                new LongestMatchColumnWidthStyleStrategy(),
                StyleStrategy.customHorizontalCellStyleStrategy(),
                new DropDownSheetWriteHandler(modelClass),
                new CommentRowWriteHandler(modelClass));
    }

}
