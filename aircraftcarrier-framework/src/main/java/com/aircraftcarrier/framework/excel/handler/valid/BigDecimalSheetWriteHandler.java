package com.aircraftcarrier.framework.excel.handler.valid;

import com.aircraftcarrier.framework.excel.annotation.valid.ExcelBigDecimal;
import com.aircraftcarrier.framework.excel.util.ExcelUtil;
import com.aircraftcarrier.framework.excel.util.Metadata;
import com.aircraftcarrier.framework.tookit.MapUtil;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * BigDecimal
 *
 * @author zhipengliu
 * @date 2025/5/3
 * @since 1.0
 */
@Slf4j
public class BigDecimalSheetWriteHandler implements SheetWriteHandler {
    private final Map<Integer, ExcelBigDecimal> map;
    private final Map<Integer, String> formulaMap;

    private static final String FORMULA = "AND(ISNUMBER(%s%d), " +
            "IF(FIND(\".\", TEXT(%s%d, \"0.00\"))>0, " +
            "LEN(LEFT(TEXT(%s%d, \"0.00\"), FIND(\".\", TEXT(%s%d, \"0.00\"))-1))<=%d, " +
            "LEN(TEXT(%s%d, \"0.00\"))<=%d), " +
            "IF(FIND(\".\", TEXT(%s%d, \"0.00\"))>0, " +
            "LEN(MID(TEXT(%s%d, \"0.00\"), FIND(\".\", TEXT(%s%d, \"0.00\"))+1, 255))<=%d, TRUE)))";

    public <T> BigDecimalSheetWriteHandler(Class<T> templateClass) {
        Field[] fields = templateClass.getDeclaredFields();
        map = MapUtil.newHashMap(fields.length);
        formulaMap = MapUtil.newHashMap(fields.length);

        Map<Integer, Metadata> indexNameMap = ExcelUtil.getIndexNameMap(1, templateClass);
        int startRow = 1;

        for (Field field : fields) {
            ExcelBigDecimal annotation = field.getAnnotation(ExcelBigDecimal.class);
            if (null != annotation) {
                indexNameMap.forEach((columnIndex, metadata) -> {
                    if (metadata.getField().getName().equals(field.getName())) {
                        // 1. 列索引转字母（如 2 → "C"）
                        String columnLetter = ExcelUtil.getColumnLetter(columnIndex);

                        String formula = String.format(FORMULA,
                                columnLetter, startRow + 1,
                                columnLetter, startRow + 1,
                                columnLetter, startRow + 1, columnLetter, startRow + 1, annotation.integer(),
                                columnLetter, startRow + 1, annotation.integer(),
                                columnLetter, startRow + 1,
                                columnLetter, startRow + 1, columnLetter, startRow + 1, annotation.scale());

                        map.put(columnIndex, annotation);
                        formulaMap.put(columnIndex, formula);
                    }
                });
            }
        }
    }


    /**
     * beforeSheetCreate
     *
     * @param writeWorkbookHolder writeWorkbookHolder
     * @param writeSheetHolder    writeSheetHolder
     */
    @Override
    public void beforeSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        log.info("beforeSheetCreate...");
    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        if (map.isEmpty()) {
            return;
        }
        // 这里可以对cell进行任何操作
        Sheet sheet = writeSheetHolder.getSheet();
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();
        // k 为存在下拉数据集的单元格下表 v为下拉数据集
        map.forEach((index, excelBigDecimal) -> {

            // 设置下拉单元格的首行 末行 首列 末列
            CellRangeAddressList rangeList = new CellRangeAddressList(1, 65536, index, index);

            // Decimal列表约束数据 min max
            DataValidationConstraint rangeConstraint = validationHelper.createDecimalConstraint(
                    DataValidationConstraint.OperatorType.BETWEEN,
                    excelBigDecimal.min(),
                    excelBigDecimal.max()
            );
            // 设置约束
            DataValidation validation = validationHelper.createValidation(rangeConstraint, rangeList);
            // 阻止输入非下拉选项的值
            validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            validation.setShowErrorBox(true);
            validation.setSuppressDropDownArrow(true);
            String message = String.format("此值与单元格定义格式min(%s), max(%s)不一致", excelBigDecimal.min(), excelBigDecimal.max());
            validation.createErrorBox("提示", message);
            sheet.addValidationData(validation);

            // TODO: 2025/5/3 integer, scale
            DataValidationConstraint digitsConstraint = validationHelper.createCustomConstraint(formulaMap.get(index));
            DataValidation digitsValidation = validationHelper.createValidation(digitsConstraint, rangeList);
            // 阻止输入非下拉选项的值
            digitsValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            digitsValidation.setShowErrorBox(true);
            digitsValidation.setSuppressDropDownArrow(true);
            message = String.format("此值与单元格定义格式integer(%s), scale(%s)不一致", excelBigDecimal.integer(), excelBigDecimal.scale());
            digitsValidation.createErrorBox("提示", message);
            sheet.addValidationData(digitsValidation);
        });
    }
}
