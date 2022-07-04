package com.aircraftcarrier.framework.excel.convert;

import com.aircraftcarrier.framework.enums.IEnum;
import com.aircraftcarrier.framework.excel.annotation.ExcelConvert;
import com.aircraftcarrier.framework.exception.SysException;
import com.aircraftcarrier.framework.tookit.MapUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * excel上传枚举code, 使用的枚举参数接受
 * 枚举需要继承IEnum
 * 并且需要配合 {@link ExcelConvert} 指定接受的枚举类型
 *
 * <pre> {@code
 * @ExcelProperty(value = "枚举演示2", converter = IEnumCodeConverter.class)
 * @ExcelConvert(sourceEnumClass = YnValueEnum.class)
 * private Integer yn;
 * }</pre>
 *
 * @author lzp
 */
public class IEnumCodeConverter implements Converter<Object> {

    private final Map<String, Map<Object, String>> codeMap = MapUtil.newHashMap(32);
    private final Map<String, Map<String, Object>> descMap = MapUtil.newHashMap(32);

    @Override
    public Class supportJavaTypeKey() {
        return Object.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public Object convertToJavaData(ReadCellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        Field field = contentProperty.getField();
        ExcelConvert annotation = field.getAnnotation(ExcelConvert.class);
        if (annotation == null || annotation.sourceEnumClass() == IEnum.class) {
            throw new SysException("The @ExcelConvert annotation is missing");
        }

        String name = annotation.sourceEnumClass().getName();
        Map<String, Object> stringObjectMap = descMap.get(name);
        if (stringObjectMap == null) {
            Map<String, Object> innerMap = MapUtil.newHashMap(32);
            Class<IEnum> anEnum = (Class<IEnum>) Class.forName(name);
            IEnum[] enumConstants = anEnum.getEnumConstants();
            for (IEnum enumConstant : enumConstants) {
                innerMap.put(enumConstant.desc(), enumConstant.code());
            }
            descMap.putIfAbsent(name, innerMap);
            stringObjectMap = descMap.get(name);
        }
        return stringObjectMap.get(cellData.getStringValue());
    }

    @Override
    public WriteCellData<String> convertToExcelData(Object value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        Field field = contentProperty.getField();
        ExcelConvert annotation = field.getAnnotation(ExcelConvert.class);
        if (annotation == null || annotation.sourceEnumClass() == IEnum.class) {
            throw new SysException("The @ExcelConvert annotation is missing");
        }

        String name = annotation.sourceEnumClass().getName();
        Map<Object, String> integerStringMap = codeMap.get(name);
        if (integerStringMap == null) {
            Map<Object, String> innerMap = MapUtil.newHashMap(32);
            Class<IEnum> anEnum = (Class<IEnum>) Class.forName(name);
            IEnum[] enumConstants = anEnum.getEnumConstants();
            for (IEnum enumConstant : enumConstants) {
                innerMap.put(enumConstant.code(), enumConstant.desc());
            }
            codeMap.putIfAbsent(name, innerMap);
            integerStringMap = codeMap.get(name);
        }
        return new WriteCellData<>(integerStringMap.get(value));
    }
}
