package com.aircraftcarrier.framework.excel.util;

import com.aircraftcarrier.framework.excel.convert.LocalDateTimeConverter;
import com.aircraftcarrier.framework.tookit.StringUtils;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.util.DateUtils;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.handler.WriteHandler;
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
     * @param pojoClass     对象Class
     * @param writeHandlers writeHandlers
     */
    public static <T> void exportExcel(HttpServletResponse response, String fileName, String sheetName, List<T> list,
                                       Class<T> pojoClass, WriteHandler... writeHandlers) throws IOException {
        if (StringUtils.isBlank(fileName)) {
            //当前日期
            fileName = DateUtils.format(new Date(), DateUtils.DATE_FORMAT_10);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("UTF-8");
        fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.displayName());
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        ExcelWriterBuilder excelWriterBuilder = EasyExcelFactory.write(response.getOutputStream(), pojoClass);
        if (writeHandlers != null && writeHandlers.length > 0) {
            for (WriteHandler writeHandler : writeHandlers) {
                excelWriterBuilder.registerWriteHandler(writeHandler);
            }
        }
        excelWriterBuilder.registerConverter(new LocalDateTimeConverter());
        excelWriterBuilder.sheet(sheetName).doWrite(list);
    }

    /**
     * Excel导出，先sourceList转换成List<targetClass>，再导出
     *
     * @param response    response
     * @param fileName    文件名
     * @param sheetName   sheetName
     * @param sourceList  原数据List
     * @param targetClass 目标对象Class
     */
    public static <S, T> void exportExcelToTarget(HttpServletResponse response, String fileName, String sheetName, List<S> sourceList,
                                                  Class<T> targetClass, WriteHandler... writeHandlers) throws NoSuchMethodException, IOException, InvocationTargetException, InstantiationException, IllegalAccessException {
        List<T> targetList = new ArrayList<>(sourceList.size());
        Constructor<T> constructor = targetClass.getDeclaredConstructor();
        for (S source : sourceList) {
            T target = constructor.newInstance();
            BeanUtils.copyProperties(source, target);
            targetList.add(target);
        }

        exportExcel(response, fileName, sheetName, targetList, targetClass, writeHandlers);
    }

}
