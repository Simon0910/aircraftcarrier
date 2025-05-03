package com.aircraftcarrier.framework.excel.util;

import com.aircraftcarrier.framework.exception.BizException;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.util.ListUtils;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * ExcelUtil
 *
 * @author zhipengliu
 * @date 2025/4/20
 * @since 1.0
 */
public class ExcelUtil {


    /**
     * 分隔符
     */
    private static final String POINT = ".";


    /**
     * 将列索引转换为Excel列字母（如 0→A, 1→B, 26→AA）
     */
    public static String getColumnLetter(int columnIndex) {
        if (columnIndex < 0) {
            throw new IllegalArgumentException("列索引不能为负数");
        }
        StringBuilder columnName = new StringBuilder();
        while (columnIndex >= 0) {
            int remainder = columnIndex % 26;
            columnName.insert(0, (char) ('A' + remainder));
            columnIndex = (columnIndex / 26) - 1;
        }
        return columnName.toString();
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
        if (originalFilename == null || !ExcelTypeEnum.XLSX.getValue().equals(
                originalFilename.substring(originalFilename.lastIndexOf(POINT)))) {
            throw new BizException("格式只支持xlsx");
        }
    }


    /**
     * getIndexNameMap
     * 通过class获取类字段信息
     *
     * @param clazz clazz
     * @return Map
     */
    public static Map<Integer, Metadata> getIndexNameMap(int headIndex, Class<?> clazz) {
        // 获取类中所有的属性
        Field[] fields = clazz.getDeclaredFields();
        Map<Integer, List<Metadata>> orderFieldMap = new TreeMap<>();
        Map<Integer, Metadata> indexFieldMap = new TreeMap<>();
        ExcelIgnoreUnannotated excelIgnoreUnannotated = clazz.getAnnotation(ExcelIgnoreUnannotated.class);
        for (Field field : fields) {
            declaredOneField(headIndex, field, orderFieldMap, indexFieldMap, excelIgnoreUnannotated);
        }
        return buildSortedAllFieldMap(orderFieldMap, indexFieldMap);
    }

    private static Map<Integer, Metadata> buildSortedAllFieldMap(Map<Integer, List<Metadata>> orderFieldMap,
                                                                 Map<Integer, Metadata> indexFieldMap) {
        Map<Integer, Metadata> sortedAllFieldMap = new HashMap<>(
                (orderFieldMap.size() + indexFieldMap.size()) * 4 / 3 + 1);

        Map<Integer, Metadata> tempIndexFieldMap = new HashMap<>(indexFieldMap);
        int index = 0;
        for (List<Metadata> fieldList : orderFieldMap.values()) {
            for (Metadata field : fieldList) {
                while (tempIndexFieldMap.containsKey(index)) {
                    sortedAllFieldMap.put(index, tempIndexFieldMap.get(index));
                    tempIndexFieldMap.remove(index);
                    index++;
                }
                sortedAllFieldMap.put(index, field);
                index++;
            }
        }
        sortedAllFieldMap.putAll(tempIndexFieldMap);
        return sortedAllFieldMap;
    }

    private static void declaredOneField(int headIndex, Field field,
                                         Map<Integer, List<Metadata>> orderFieldMap,
                                         Map<Integer, Metadata> indexFieldMap,
                                         ExcelIgnoreUnannotated excelIgnoreUnannotated) {
        ExcelIgnore excelIgnore = field.getAnnotation(ExcelIgnore.class);
        if (excelIgnore != null) {
            return;
        }
        ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
        boolean noExcelProperty = excelProperty == null && excelIgnoreUnannotated != null;
        if (noExcelProperty) {
            return;
        }
        boolean isStaticFinalOrTransient =
                (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()))
                        || Modifier.isTransient(field.getModifiers());
        if (excelProperty == null && isStaticFinalOrTransient) {
            return;
        }
        if (excelProperty != null && excelProperty.index() >= 0) {
            String excelHeaderName = getHeaderName(headIndex, field, excelProperty);
            indexFieldMap.put(excelProperty.index(), Metadata.builder().field(field).headName(excelHeaderName).build());
            return;
        }

        int order = Integer.MAX_VALUE;
        if (excelProperty != null) {
            order = excelProperty.order();
        }
        List<Metadata> orderFieldList = orderFieldMap.computeIfAbsent(order, key -> ListUtils.newArrayList());
        if (excelProperty != null) {
            String excelHeaderName = getHeaderName(headIndex, field, excelProperty);
            orderFieldList.add(Metadata.builder().field(field).headName(excelHeaderName).build());
        } else {
            orderFieldList.add(Metadata.builder().field(field).headName(field.getName()).build());
        }
    }


    private static String getHeaderName(int headIndex, Field field, ExcelProperty excelProperty) {
        String value;
        int valueIndex = excelProperty.value().length - 1;
        if (valueIndex > headIndex) {
            value = excelProperty.value()[headIndex];
        } else {
            value = excelProperty.value()[valueIndex];
        }
        value = "".equals(value) ? field.getName() : value;
        if (cn.easyes.common.utils.StringUtils.isBlank(value)) {
            return null;
        }
        return value;
    }
}
