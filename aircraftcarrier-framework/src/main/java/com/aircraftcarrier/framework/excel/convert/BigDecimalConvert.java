package com.aircraftcarrier.framework.excel.convert;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class BigDecimalConvert implements Converter<BigDecimal> {

    private static final String COMMA = ",";
    private static final String EMPTY = "";

    private final DecimalFormat decimalFormat;

    public BigDecimalConvert() {
        this("#,##0.00");
    }

    public BigDecimalConvert(String pattern) {
        decimalFormat = new DecimalFormat(pattern);
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
    }

    @Override
    public Class<BigDecimal> supportJavaTypeKey() {
        return BigDecimal.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }


    @Override
    public BigDecimal convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                                        GlobalConfiguration globalConfiguration) throws Exception {
        if (cellData.getStringValue().contains(COMMA)) {
            return new BigDecimal(cellData.getStringValue().replace(COMMA, EMPTY))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(cellData.getStringValue()).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public WriteCellData<BigDecimal> convertToExcelData(BigDecimal value, ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) throws Exception {
        return new WriteCellData<>(decimalFormat.format(value));
    }
}