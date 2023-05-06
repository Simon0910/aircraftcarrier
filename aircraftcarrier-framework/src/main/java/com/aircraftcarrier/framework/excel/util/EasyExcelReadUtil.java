package com.aircraftcarrier.framework.excel.util;

import com.aircraftcarrier.framework.exception.BizException;
import com.aircraftcarrier.framework.exception.ErrorCode;
import com.aircraftcarrier.framework.exception.ToolException;
import com.aircraftcarrier.framework.tookit.MapUtil;
import com.aircraftcarrier.framework.tookit.StringUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
     * 分隔符
     */
    private static final String POINT = ".";

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
    private static void doRead(ExcelReaderBuilder builder, Integer startSheetNo, Integer endSheetNo, Integer headRowNumber) {
        ExcelReader excelReader = builder.headRowNumber(headRowNumber).autoTrim(true).build();
        if (startSheetNo == null || endSheetNo == null) {
            excelReader.readAll();
            return;
        }
        for (int start = startSheetNo; start <= endSheetNo; start++) {
            ReadSheet readSheet = EasyExcelFactory.readSheet(start).headRowNumber(headRowNumber).build();
            excelReader.read(readSheet);
        }
        excelReader.finish();
    }


    /**
     * 校验excel 只支持xlsx
     *
     * @param file file
     * @throws RuntimeException RuntimeException
     */
    public static void checkExcelFile(MultipartFile file) throws BizException {
        if (file == null) {
            throw new BizException("file 不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !ExcelTypeEnum.XLSX.getValue().equals(originalFilename.substring(originalFilename.lastIndexOf(POINT)))) {
            throw new BizException("格式只支持xlsx");
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

    public static boolean allFieldIsNull(ExcelRow o) {
        for (Field field : o.getClass().getDeclaredFields()) {
            ReflectionUtils.makeAccessible(field);

            Object object;
            try {
                object = field.get(o);
            } catch (IllegalAccessException e) {
                log.error("field.get(o) error: ", e);
                throw new ToolException(ErrorCode.INTERNAL_SERVER_ERROR, "field.get(o) error");
            }
            if (object instanceof CharSequence) {
                if (!org.springframework.util.ObjectUtils.isEmpty(object)) {
                    return false;
                }
            } else {
                if (null != object) {
                    return false;
                }
            }
        }
        return true;
    }

    private static <T extends ExcelRow> AnalysisEventListener<T> buildAnalysisEventListener(LinkedHashSet<T> rowList, LinkedHashMap<Integer, String> errors) {
        return new AnalysisEventListener<T>() {
            boolean isEmpty = true;

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
                if (exception instanceof ExcelDataConvertException excelDataConvertException) {
                    log.error("第{}行，第{}列解析异常", excelDataConvertException.getRowIndex(), excelDataConvertException.getColumnIndex(), excelDataConvertException);
                    errors.put(excelDataConvertException.getRowIndex(), MessageFormat.format("第{}行，第{}列解析异常", excelDataConvertException.getRowIndex(), excelDataConvertException.getColumnIndex()));
                } else {
                    throw exception;
                }
            }
        };
    }

    private static <T extends ExcelRow> AnalysisEventListener<T> buildAnalysisEventListener(SingleRowListener<T> singleRowListener, Class<T> model) {
        return new AnalysisEventListener<T>() {
            boolean isEmpty = true;

            @Override
            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                boolean successful = singleRowListener.invokeHeadMap(headMap, context);
                if (!successful) {
                    checkHead(headMap, model);
                }
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
                singleRowListener.onException(exception, analysisContext);
            }
        };
    }

    private static <T extends ExcelRow> AnalysisEventListener<T> buildAnalysisEventListener(BatchRowListener<T> batchRowListener, final Class<T> model) {
        return new AnalysisEventListener<T>() {
            boolean isEmpty = true;
            LinkedHashSet<T> rowSet = new LinkedHashSet<>(BATCH_SIZE);

            @Override
            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                boolean successful = batchRowListener.invokeHeadMap(headMap, context);
                if (!successful) {
                    checkHead(headMap, model);
                }
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
                rowSet.add(row);
                if (rowSet.size() == BATCH_SIZE) {
                    ArrayList<T> list = new ArrayList<>(rowSet);
                    rowSet = new LinkedHashSet<>(BATCH_SIZE);
                    batchRowListener.batchInvoke(list, analysisContext);
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                if (isEmpty) {
                    throw new BizException(EMPTY_FILE);
                }
                if (!rowSet.isEmpty()) {
                    ArrayList<T> list = new ArrayList<>(rowSet);
                    batchRowListener.batchInvoke(list, analysisContext);
                }
                rowSet = null;
                batchRowListener.doAfterAllAnalysed(analysisContext);
            }

            @Override
            public void onException(Exception exception, AnalysisContext analysisContext) throws Exception {
                batchRowListener.onException(exception, analysisContext);
            }
        };
    }

    /**
     * checkHead
     *
     * @param headMap headMap
     * @param model   model
     */
    private static <T extends ExcelRow> void checkHead(Map<Integer, String> headMap, Class<T> model) {
        Map<Integer, String> head = MapUtil.newHashMap();
        try {
            // 通过class获取到使用@ExcelProperty注解配置的字段
            head = getIndexNameMap(model);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        // 解析到的excel表头和实体配置的进行比对
        Set<Integer> keySet = head.keySet();
        for (Integer key : keySet) {
            if (StringUtil.isBlank(headMap.get(key))) {
                log.error("表头第" + key + 1 + "列为空，请参照模板填写");
                throw new BizException("解析excel出错，请传入正确格式的excel");
            }
            if (!headMap.get(key).equals(head.get(key))) {
                log.error("表头第" + key + 1 + "列【" + headMap.get(key) + "】与模板【" + head.get(key) + "】不一致，请参照模板填写");
                throw new BizException("解析excel出错，请传入正确格式的excel");
            }
        }
    }

    /**
     * getIndexNameMap
     * 通过class获取类字段信息
     *
     * @param clazz clazz
     * @return Map
     * @throws NoSuchFieldException NoSuchFieldException
     */
    private static Map<Integer, String> getIndexNameMap(Class<?> clazz) throws NoSuchFieldException {
        // 获取类中所有的属性
        Field[] fields = clazz.getDeclaredFields();
        Map<Integer, String> result = MapUtil.newHashMap(fields.length);
        int index = 0;
        for (Field item : fields) {
            Field field = clazz.getDeclaredField(item.getName());
            ReflectionUtils.makeAccessible(field);
            // 获取根据注解的方式获取ExcelProperty修饰的字段
            ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
            if (excelProperty != null) {
                StringBuilder stringBuilder = new StringBuilder();
                // 字段值
                for (String v : excelProperty.value()) {
                    stringBuilder.append(v);
                }
                result.put(index, stringBuilder.toString());
                index++;
            }
        }
        return result;
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
    public static <T extends ExcelRow> ReadResult<T> readAllList(InputStream inputStream, Class<T> className, Integer headRowNumber) {
        return EasyExcelReadUtil.readAllList(inputStream, className, null, null, headRowNumber);
    }

    /**
     * 读取excel到list，startSheetNo、endSheetNo为null，读取所有sheet，不为null，读取对应的sheet，注意，下标从0开始
     *
     * @param inputStream   inputStream
     * @param className     className
     * @param startSheetNo  startSheetNo，注意，下标从0开始
     * @param endSheetNo    endSheetNo，注意，下标从0开始
     * @param headRowNumber headRowNumber，表头共占用几行
     * @return list
     * @throws RuntimeException RuntimeException
     */
    public static <T extends ExcelRow> ReadResult<T> readAllList(InputStream inputStream, Class<T> className, Integer startSheetNo, Integer endSheetNo, Integer headRowNumber) {
        LinkedHashMap<Integer, String> errors = new LinkedHashMap<>();
        LinkedHashSet<T> rowList = new LinkedHashSet<>();
        ExcelReaderBuilder builder = EasyExcelFactory.read(inputStream, className, EasyExcelReadUtil.buildAnalysisEventListener(rowList, errors));
        EasyExcelReadUtil.doRead(builder, startSheetNo, endSheetNo, headRowNumber);
        ReadResult<T> readResult = new ReadResult<>();
        readResult.setRowList(new ArrayList<>(rowList));
        readResult.setErrors(errors);
        return readResult;
    }

    /**
     * 读取excel单行处理，startSheetNo、endSheetNo为null，读取所有sheet，不为null，读取对应的sheet，注意，下标从0开始
     *
     * @param inputStream       inputStream
     * @param startSheetNo      startSheetNo，注意，下标从0开始
     * @param endSheetNo        endSheetNo，注意，下标从0开始
     * @param singleRowListener singleRowListener
     * @throws RuntimeException RuntimeException
     */
    public static <T extends ExcelRow> void readSingleRow(InputStream inputStream, Class<T> model, Integer startSheetNo, Integer endSheetNo, SingleRowListener<T> singleRowListener) {
        EasyExcelReadUtil.readSingleRow(inputStream, model, startSheetNo, endSheetNo, 1, singleRowListener);
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
    public static <T extends ExcelRow> void readSingleRow(InputStream inputStream, Class<T> model, Integer startSheetNo, Integer endSheetNo, Integer headRowNumber, SingleRowListener<T> singleRowListener) {
        ExcelReaderBuilder builder = EasyExcelFactory.read(inputStream, model, buildAnalysisEventListener(singleRowListener, model));
        EasyExcelReadUtil.doRead(builder, startSheetNo, endSheetNo, headRowNumber);
    }


    /**
     * 读取excel多行批次处理，读取所有sheet，注意，下标从0开始
     *
     * @param inputStream      inputStream
     * @param headRowNumber    headRowNumber，注意，表头共占用几行
     * @param batchRowListener batchListener
     * @throws RuntimeException RuntimeException
     */
    public static <T extends ExcelRow> void readBatchRow(InputStream inputStream, Class<T> model, Integer headRowNumber, BatchRowListener<T> batchRowListener) {
        EasyExcelReadUtil.readBatchRow(inputStream, model, null, null, headRowNumber, batchRowListener);
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
    public static <T extends ExcelRow> void readBatchRow(InputStream inputStream, Class<T> model, Integer startSheetNo, Integer endSheetNo, Integer headRowNumber, BatchRowListener<T> batchRowListener) {
        ExcelReaderBuilder builder = EasyExcelFactory.read(inputStream, model, buildAnalysisEventListener(batchRowListener, model));
        EasyExcelReadUtil.doRead(builder, startSheetNo, endSheetNo, headRowNumber);
    }

    /**
     * 读取excel多行批次处理，startSheetNo、endSheetNo为null，读取所有sheet，不为null，读取对应的sheet，注意，下标从0开始
     * <pre> {@code
     *  EasyExcelReadUtil.lambdaReadBatchRow(file.getInputStream(), DouDianProductImportExcel.class, 0, 0, 1, (list, context) -> {
     *      System.out.println(JSON.toJSONString(list));
     *  });
     * }</pre>
     *
     * @param inputStream      inputStream
     * @param startSheetNo     startSheetNo，注意，下标从0开始
     * @param endSheetNo       endSheetNo，注意，下标从0开始
     * @param headRowNumber    headRowNumber，注意，表头共占用几行
     * @param batchRowListener batchRowListener
     * @throws RuntimeException RuntimeException
     */
    public static <T extends ExcelRow> void lambdaReadBatchRow(InputStream inputStream, Class<T> model, Integer startSheetNo, Integer endSheetNo, Integer headRowNumber, BatchRowListener<T> batchRowListener) {
        ExcelReaderBuilder builder = EasyExcelFactory.read(inputStream, model, buildAnalysisEventListener(batchRowListener, model));
        EasyExcelReadUtil.doRead(builder, startSheetNo, endSheetNo, headRowNumber);
    }
}
