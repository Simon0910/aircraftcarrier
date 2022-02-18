package com.aircraftcarrier.framework.excel.util;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;
import java.util.List;

/**
 * @author lzp
 */
@Slf4j
public class ExcelWriteUtil {
    /**
     * EasyExcelWriteUtil
     */
    private ExcelWriteUtil() {
    }

    /**
     * 动态创建数据表格
     *
     * @param dynamicHeads heads
     * @param data         data
     * @param sheetName    sheetName
     * @return java.io.ByteArrayOutputStream
     */
    public static <T extends OutputStream> T writeByDynamicHeads(List<List<String>> dynamicHeads, List<List<Object>> data, String sheetName, T outputStream) {
        EasyExcelFactory.write(outputStream)
                .head(dynamicHeads)
                .autoCloseStream(Boolean.FALSE)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet(sheetName)
                .doWrite(data);
        return outputStream;
    }

    /**
     * data ==> InputStream
     *
     * @param sheetInstants sheetInstants
     * @return java.io.ByteArrayInputStream
     */
    public static <E, T extends OutputStream> T writeManySheet(List<SheetInstant<E>> sheetInstants, T outputStream) {
        ExcelWriter excelWriter = EasyExcelFactory.write(outputStream)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .build();

        WriteSheet writeSheet;
        try {
            for (int i = 0; i < sheetInstants.size(); i++) {
                SheetInstant<E> sheetInstant = sheetInstants.get(i);
                writeSheet = EasyExcelFactory.writerSheet(i, sheetInstant.getSheetName()).head(sheetInstant.getHeadType()).build();
                excelWriter.write(sheetInstant.getDataList(), writeSheet);
            }
        } catch (Exception e) {
            log.error("创建excel异常: ", e);
            throw e;
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }

        return outputStream;
    }
}
