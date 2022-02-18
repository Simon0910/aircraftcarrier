package com.aircraftcarrier.framework.excel.convert;

import com.aircraftcarrier.framework.enums.IEnum;
import com.aircraftcarrier.framework.tookit.MapUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.util.Map;

/**
 * @author lzp
 */
public class IEnumConverter<T extends IEnum> implements Converter<T> {

    private final Map<String, Map<String, T>> cached = MapUtil.newHashMap();

    @Override
    public Class supportJavaTypeKey() {
        return IEnum.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public T convertToJavaData(CellData cellData, ExcelContentProperty excelContentProperty,
                               GlobalConfiguration globalConfiguration) throws ClassNotFoundException {

        String name = excelContentProperty.getField().getType().getName();

        Map<String, T> stringEnumMap = cached.get(name);
        if (stringEnumMap == null) {
            cached.computeIfAbsent(name, k -> MapUtil.newHashMap());
            stringEnumMap = cached.get(name);
        }

        T iEnum = stringEnumMap.get(cellData.getStringValue());
        if (iEnum == null) {
            Class<T> anEnum = (Class<T>) Class.forName(name);
            T[] enumConstants = anEnum.getEnumConstants();
            for (T enumConstant : enumConstants) {
                stringEnumMap.put(enumConstant.desc(), enumConstant);
            }
            return stringEnumMap.get(cellData.getStringValue());
        }

        return iEnum;
    }

    @Override
    public CellData<String> convertToExcelData(T iEnum, ExcelContentProperty excelContentProperty,
                                               GlobalConfiguration globalConfiguration) {
        return new CellData<>(iEnum.desc());
    }
}
