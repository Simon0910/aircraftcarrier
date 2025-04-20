package com.aircraftcarrier.framework.excel.util;

import com.aircraftcarrier.framework.excel.convert.BigDecimalConvert;
import com.aircraftcarrier.framework.exception.BizException;
import com.aircraftcarrier.framework.exception.ErrorCode;
import com.aircraftcarrier.framework.exception.SysException;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.metadata.ReadSheet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 工具类，实现excel的读取，大数据量时无内存溢出问题，格式只支持xlsx
 *
 * @author lishudong
 * @version 1.0
 * @date 2019/11/15
 */
@Slf4j
public class EasyExcelReadUtil {

    /**
     * empty file
     */
    private static final String EMPTY_FILE = "上传文件为空";

    /**
     * 默认批次大小
     */
    private static final int BATCH_SIZE = 1000;

    private EasyExcelReadUtil() {
    }


    /**
     * @param builder       builder
     * @param startSheetNo  startSheetNo，注意，下标从0开始
     * @param endSheetNo    endSheetNo，注意，下标从0开始
     * @param headRowNumber headRowNumber，表头共占用几行
     */
    private static void doRead(ExcelReaderBuilder builder,
                               Integer startSheetNo,
                               Integer endSheetNo,
                               Integer headRowNumber) throws Exception {
        // read excel convert
        builder.registerConverter(new BigDecimalConvert());

        try (ExcelReader excelReader = builder.headRowNumber(headRowNumber).autoTrim(true).build()) {
            if (startSheetNo == null || endSheetNo == null) {
                excelReader.readAll();
                return;
            }
            for (int start = startSheetNo; start <= endSheetNo; start++) {
                ReadSheet readSheet = EasyExcelFactory.readSheet(start).headRowNumber(headRowNumber).build();
                excelReader.read(readSheet);
            }
            // excelReader.finish();
        }
    }

    /**
     * 获取当前行号
     *
     * @param analysisContext analysisContext
     * @return 当前行号
     */
    public static int getCurrentRowIndex(AnalysisContext analysisContext) {
        return analysisContext.readRowHolder().getRowIndex() + 1;
    }

    public static boolean allFieldIsNull(ExcelRow row) {
        for (Field field : row.getClass().getDeclaredFields()) {
            ReflectionUtils.makeAccessible(field);

            Object object;
            try {
                object = field.get(row);
            } catch (IllegalAccessException e) {
                log.error("field.get(row) error: ", e);
                throw new SysException(ErrorCode.INTERNAL_SERVER_ERROR, "field.get(row) error");
            }
            if (object instanceof CharSequence cs && StringUtils.hasText(cs)) {
                return false;
            }
            if (object != null) {
                return false;
            }
        }
        return true;
    }

    private static <T extends ExcelRow> AnalysisEventListener<T> buildAnalysisEventListener(Class<T> head, List<T> rowList) {
        return new AnalysisEventListener<>() {
            boolean isEmpty = true;
            int headIndex = 0;

            @Override
            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                checkHead(headIndex++, headMap, head);
            }

            @Override
            public void invoke(T row, AnalysisContext analysisContext) {
                if (EasyExcelReadUtil.allFieldIsNull(row)) {
                    return;
                }
                if (isEmpty) {
                    isEmpty = false;
                }
                row.setRowNo(EasyExcelReadUtil.getCurrentRowIndex(analysisContext));
                rowList.add(row);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                log.info("doAfterAllAnalysed...");
                if (isEmpty) {
                    throw new BizException(EMPTY_FILE);
                }
            }

            @Override
            public void onException(Exception exception, AnalysisContext analysisContext) throws Exception {
                throw exception;
            }
        };
    }

    private static <T extends ExcelRow> AnalysisEventListener<T> buildAnalysisEventListener(SingleRowListener<T> singleRowListener, Class<T> head) {
        return new AnalysisEventListener<>() {
            boolean isEmpty = true;
            int headIndex = 0;

            @Override
            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                checkHead(headIndex++, headMap, head);
                singleRowListener.invokeHeadMap(headMap, context);
            }

            @Override
            public void invoke(T row, AnalysisContext analysisContext) {
                if (EasyExcelReadUtil.allFieldIsNull(row)) {
                    return;
                }
                if (isEmpty) {
                    isEmpty = false;
                }
                row.setRowNo(EasyExcelReadUtil.getCurrentRowIndex(analysisContext));
                singleRowListener.invoke(row, analysisContext);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                log.info("doAfterAllAnalysed...");
                if (isEmpty) {
                    throw new BizException(EMPTY_FILE);
                }
                singleRowListener.doAfterAllAnalysed(analysisContext);
            }

            @Override
            public void onException(Exception exception, AnalysisContext analysisContext) throws Exception {
                try {
                    singleRowListener.onException(exception, analysisContext);
                } catch (Exception ignore) {
                }
                throw exception;
            }
        };
    }

    private static <T extends ExcelRow> AnalysisEventListener<T> buildAnalysisEventListener(BatchRowListener<T> batchRowListener, final Class<T> head) {
        return new AnalysisEventListener<>() {
            boolean isEmpty = true;
            int headIndex = 0;
            List<T> rowList = new ArrayList<>(BATCH_SIZE);

            @Override
            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                checkHead(headIndex++, headMap, head);
                batchRowListener.invokeHeadMap(headMap, context);
            }

            @Override
            public void invoke(T row, AnalysisContext analysisContext) {
                if (EasyExcelReadUtil.allFieldIsNull(row)) {
                    return;
                }
                if (isEmpty) {
                    isEmpty = false;
                }
                row.setRowNo(EasyExcelReadUtil.getCurrentRowIndex(analysisContext));
                rowList.add(row);
                if (rowList.size() == BATCH_SIZE) {
                    batchRowListener.batchInvoke(rowList, analysisContext);
                    rowList.clear();
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                if (isEmpty) {
                    throw new BizException(EMPTY_FILE);
                }
                if (!rowList.isEmpty()) {
                    batchRowListener.batchInvoke(rowList, analysisContext);
                    rowList.clear();
                }
                rowList = null;
                batchRowListener.doAfterAllAnalysed(analysisContext);
            }

            @Override
            public void onException(Exception exception, AnalysisContext analysisContext) throws Exception {
                try {
                    batchRowListener.onException(exception, analysisContext);
                } catch (Exception ignore) {
                }
                throw exception;
            }
        };
    }

    /**
     * checkHead
     *
     * @param headMap headMap
     * @param head    head
     */
    private static <T extends ExcelRow> void checkHead(int headIndex, Map<Integer, String> headMap, Class<T> head) {
        Map<Integer, Metadata> classHeadNameMap = ExcelUtil.getIndexNameMap(headIndex, head);
        // 解析到的excel表头和实体配置的进行比对
        classHeadNameMap.forEach((key, value) -> {
            if (!Objects.equals(headMap.get(key), value.getHeadName())) {
                log.error("表头第" + (key + 1) + "列【" + headMap.get(key) + "】与模板【" + value + "】不一致，请参照模板填写");
                throw new BizException("解析excel出错，请传入正确格式的excel");
            }
        });
    }


    /**
     * 读取excel到list，读取所有sheet
     *
     * @param inputStream inputStream
     * @param className   className
     * @return list
     * @throws RuntimeException RuntimeException
     */
    public static <T extends ExcelRow> ReadResult<T> readAllList(InputStream inputStream,
                                                                 Class<T> className) {
        return EasyExcelReadUtil.readAllList(inputStream, className,
                null, null,
                1);
    }

    /**
     * 读取excel到list，读取所有sheet
     *
     * @param inputStream   inputStream
     * @param className     className
     * @param headRowNumber headRowNumber，表头共占用几行
     * @return list
     * @throws RuntimeException RuntimeException
     */
    public static <T extends ExcelRow> ReadResult<T> readAllList(InputStream inputStream,
                                                                 Class<T> className,
                                                                 Integer headRowNumber) {
        return EasyExcelReadUtil.readAllList(inputStream, className,
                null, null,
                headRowNumber);
    }

    /**
     * 读取excel到list，startSheetNo、endSheetNo为null，读取所有sheet，不为null，读取对应的sheet，注意，下标从0开始
     *
     * @param inputStream   inputStream
     * @param head          head
     * @param startSheetNo  startSheetNo，注意，下标从0开始
     * @param endSheetNo    endSheetNo，注意，下标从0开始
     * @param headRowNumber headRowNumber，表头共占用几行
     * @return list
     * @throws RuntimeException RuntimeException
     */
    public static <T extends ExcelRow> ReadResult<T> readAllList(InputStream inputStream, Class<T> head,
                                                                 Integer startSheetNo, Integer endSheetNo,
                                                                 Integer headRowNumber) {
        ReadResult<T> readResult = new ReadResult<>();

        ExcelReaderBuilder builder = EasyExcelFactory.read(inputStream, head,
                EasyExcelReadUtil.buildAnalysisEventListener(head, readResult.getRowList()));

        try {
            EasyExcelReadUtil.doRead(builder, startSheetNo, endSheetNo, headRowNumber);
        } catch (Exception e) {
            if (e instanceof ExcelDataConvertException ee) {
                int row = ee.getRowIndex() + 1;
                int columnIndex = ee.getColumnIndex() + 1;
                log.error("第{}行，第{}列解析异常 {}", row, columnIndex, ee.getCause().getMessage(), ee);
                readResult.putError(ee.getRowIndex(),
                        MessageFormat.format("第{0}行，第{1}列解析异常: {2}",
                                row, columnIndex, ee.getCause().getMessage()));
            } else {
                readResult.putError(-1, MessageFormat.format("excel read error: {0}", e.getMessage()));
            }
        }
        return readResult;
    }

    /**
     * 读取excel单行处理，startSheetNo、endSheetNo为null，读取所有sheet，不为null，读取对应的sheet，注意，下标从0开始
     *
     * @param inputStream       inputStream
     * @param singleRowListener singleRowListener
     * @throws RuntimeException RuntimeException
     */
    public static <T extends ExcelRow> void readSingleRow(InputStream inputStream, Class<T> head,
                                                          SingleRowListener<T> singleRowListener) throws Exception {
        EasyExcelReadUtil.readSingleRow(inputStream, head,
                null, null,
                1,
                singleRowListener);
    }

    /**
     * 读取excel单行处理，startSheetNo、endSheetNo为null，读取所有sheet，不为null，读取对应的sheet，注意，下标从0开始
     *
     * @param inputStream       inputStream
     * @param singleRowListener singleRowListener
     * @throws RuntimeException RuntimeException
     */
    public static <T extends ExcelRow> void readSingleRow(InputStream inputStream, Class<T> head,
                                                          Integer headRowNumber,
                                                          SingleRowListener<T> singleRowListener) throws Exception {
        EasyExcelReadUtil.readSingleRow(inputStream, head,
                null, null,
                headRowNumber,
                singleRowListener);
    }

    /**
     * 读取excel单行处理，startSheetNo、endSheetNo为null，读取所有sheet，不为null，读取对应的sheet，注意，下标从0开始
     *
     * @param inputStream       inputStream
     * @param startSheetNo      startSheetNo，注意，下标从0开始
     * @param endSheetNo        endSheetNo，注意，下标从0开始
     * @param headRowNumber     headRowNumber，注意，表头共占用几行
     * @param singleRowListener singleRowListener
     * @throws RuntimeException RuntimeException
     */
    public static <T extends ExcelRow> void readSingleRow(InputStream inputStream, Class<T> head,
                                                          Integer startSheetNo, Integer endSheetNo,
                                                          Integer headRowNumber,
                                                          SingleRowListener<T> singleRowListener) throws Exception {
        ExcelReaderBuilder builder = EasyExcelFactory.read(inputStream, head,
                buildAnalysisEventListener(singleRowListener, head));
        EasyExcelReadUtil.doRead(builder, startSheetNo, endSheetNo, headRowNumber);
    }

    /**
     * 读取excel多行批次处理，读取所有sheet，注意，下标从0开始
     *
     * @param inputStream      inputStream
     * @param batchRowListener batchListener
     * @throws RuntimeException RuntimeException
     */
    public static <T extends ExcelRow> void readBatchRow(InputStream inputStream, Class<T> head,
                                                         BatchRowListener<T> batchRowListener) throws Exception {
        EasyExcelReadUtil.readBatchRow(inputStream, head,
                null, null,
                1,
                batchRowListener);
    }

    /**
     * 读取excel多行批次处理，读取所有sheet，注意，下标从0开始
     *
     * @param inputStream      inputStream
     * @param headRowNumber    headRowNumber，注意，表头共占用几行
     * @param batchRowListener batchListener
     * @throws RuntimeException RuntimeException
     */
    public static <T extends ExcelRow> void readBatchRow(InputStream inputStream, Class<T> head,
                                                         Integer headRowNumber,
                                                         BatchRowListener<T> batchRowListener) throws Exception {
        EasyExcelReadUtil.readBatchRow(inputStream, head,
                null, null,
                headRowNumber,
                batchRowListener);
    }

    /**
     * 读取excel多行批次处理，startSheetNo、endSheetNo为null，读取所有sheet，不为null，读取对应的sheet，注意，下标从0开始
     * <pre> {@code
     * EasyExcelReadUtil.readBatchRow(file.getInputStream(), 0, 0, 1, new DouDianProductImportListener());
     * }</pre>
     *
     * @param inputStream      inputStream
     * @param startSheetNo     startSheetNo，注意，下标从0开始
     * @param endSheetNo       endSheetNo，注意，下标从0开始
     * @param headRowNumber    headRowNumber，注意，表头共占用几行
     * @param batchRowListener batchRowListener
     * @throws RuntimeException RuntimeException
     */
    public static <T extends ExcelRow> void readBatchRow(InputStream inputStream, Class<T> head,
                                                         Integer startSheetNo, Integer endSheetNo,
                                                         Integer headRowNumber,
                                                         BatchRowListener<T> batchRowListener) throws Exception {
        ExcelReaderBuilder builder = EasyExcelFactory.read(inputStream, head,
                buildAnalysisEventListener(batchRowListener, head));
        EasyExcelReadUtil.doRead(builder, startSheetNo, endSheetNo, headRowNumber);
    }
}
