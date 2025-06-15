package com.aircraftcarrier.framework.excel.convert;

import com.aircraftcarrier.framework.enums.IEnum;
import com.aircraftcarrier.framework.tookit.MapUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.util.Map;

/**
 * excel上传枚举desc汉字描述, 使用的枚举参数接受
 * 枚举需要继承IEnum
 *
 * <pre> {@code
 * @ExcelProperty(value = "枚举演示1", converter = IEnumConverter.class)
 * private DataTypeEnum dataType;
 * }</pre>
 *
 * @author lzp
 */
public class IEnumConverter implements Converter<IEnum<?>> {

    private final Map<String, Map<String, IEnum<?>>> cached = MapUtil.newHashMap();

    @Override
    public Class<?> supportJavaTypeKey() {
        return IEnum.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public IEnum<?> convertToJavaData(ReadCellData cellData, ExcelContentProperty excelContentProperty,
                                      GlobalConfiguration globalConfiguration) throws ClassNotFoundException {

        String name = excelContentProperty.getField().getType().getName();

        Map<String, IEnum<?>> stringEnumMap = cached.get(name);
        if (stringEnumMap == null) {
            cached.computeIfAbsent(name, k -> MapUtil.newHashMap());
            stringEnumMap = cached.get(name);
        }

        IEnum<?> iEnum = stringEnumMap.get(cellData.getStringValue());
        if (iEnum == null) {
            Class<IEnum<?>> anEnum = (Class<IEnum<?>>) Class.forName(name);
            IEnum<?>[] enumConstants = anEnum.getEnumConstants();
            for (IEnum<?> enumConstant : enumConstants) {
                stringEnumMap.put(enumConstant.getDesc(), enumConstant);
            }
            return stringEnumMap.get(cellData.getStringValue());
        }

        return iEnum;
    }

    @Override
    public WriteCellData<String> convertToExcelData(IEnum iEnum, ExcelContentProperty excelContentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        return new WriteCellData<>(iEnum.getDesc());
    }
}
