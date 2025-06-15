package com.aircraftcarrier.framework.excel.handler.valid;

import com.aircraftcarrier.framework.excel.annotation.valid.ExcelNumber;
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
 * Number
 *
 * @author zhipengliu
 * @date 2025/5/3
 * @since 1.0
 */
@Slf4j
public class NumberSheetWriteHandler implements SheetWriteHandler {
    private final Map<Integer, ExcelNumber> map;

    public <T> NumberSheetWriteHandler(Class<T> templateClass) {
        Field[] fields = templateClass.getDeclaredFields();
        map = MapUtil.newHashMap(fields.length);

        Map<Integer, Metadata> indexNameMap = ExcelUtil.getIndexNameMap(1, templateClass);

        for (Field field : fields) {
            ExcelNumber annotation = field.getAnnotation(ExcelNumber.class);
            if (null != annotation) {
                indexNameMap.forEach((index, metadata) -> {
                    if (metadata.getField().getName().equals(field.getName())) {
                        map.put(index, annotation);
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
        map.forEach((index, excelNumber) -> {
            // Numeric列表约束数据
            DataValidationConstraint constraint = validationHelper.createNumericConstraint(
                    excelNumber.validationType(),
                    excelNumber.operatorType(),
                    Integer.toString(excelNumber.formula1()),
                    Integer.toString(excelNumber.formula2()));
            // 设置下拉单元格的首行 末行 首列 末列
            CellRangeAddressList rangeList = new CellRangeAddressList(1, 65536, index, index);
            // 设置约束
            DataValidation validation = validationHelper.createValidation(constraint, rangeList);
            // 阻止输入非下拉选项的值
            validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            validation.setShowErrorBox(true);
            validation.setSuppressDropDownArrow(true);
            String message = String.format("此值与单元格定义格式(%s, %s)不一致", excelNumber.formula1(), excelNumber.formula2());
            validation.createErrorBox("提示", message);
            sheet.addValidationData(validation);
        });
    }
}
