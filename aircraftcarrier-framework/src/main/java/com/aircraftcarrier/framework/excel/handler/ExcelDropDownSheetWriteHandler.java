package com.aircraftcarrier.framework.excel.handler;

import com.aircraftcarrier.framework.enums.IEnum;
import com.aircraftcarrier.framework.excel.annotation.ExcelDropDown;
import com.aircraftcarrier.framework.excel.util.ExcelUtil;
import com.aircraftcarrier.framework.excel.util.Metadata;
import com.aircraftcarrier.framework.tookit.ApplicationContextUtil;
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
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * @author lzp
 */
@Slf4j
public class ExcelDropDownSheetWriteHandler implements SheetWriteHandler {

    private final Map<Integer, String[]> map;

    public <T> ExcelDropDownSheetWriteHandler(Class<T> templateClass) {
        Field[] fields = templateClass.getDeclaredFields();
        map = MapUtil.newHashMap(fields.length);

        Map<Integer, Metadata> indexNameMap = ExcelUtil.getIndexNameMap(1, templateClass);

        for (Field field : fields) {
            ExcelDropDown annotation = field.getAnnotation(ExcelDropDown.class);
            if (null != annotation) {
                String[] sources = resolve(annotation);
                indexNameMap.forEach((index, metadata) -> {
                    if (metadata.getField().getName().equals(field.getName())) {
                        map.put(index, sources);
                    }
                });
            }
        }
    }

    private static String[] resolve(ExcelDropDown excelDropDown) {
        if (Optional.ofNullable(excelDropDown).isEmpty()) {
            return new String[]{};
        }

        // 获取固定下拉信息
        String[] source = excelDropDown.source();
        if (source.length > 0) {
            return source;
        }

        // 通过Enum获取下拉对象
        Class<? extends IEnum> enumClass = excelDropDown.sourceEnumClass();
        if (enumClass.isEnum() && IEnum.class != enumClass) {
            IEnum<?>[] enumConstants = enumClass.getEnumConstants();
            return Arrays.stream(enumConstants).map(IEnum::getDesc).toArray(String[]::new);
        }

        // 获取动态的下拉数据
        Class<? extends DropDownInterface> dropDownClass = excelDropDown.sourceClass();
        if (DropDownInterface.class != dropDownClass) {
            DropDownInterface dropDownInterface = ApplicationContextUtil.getBean(dropDownClass);
            String[] dynamicSource = dropDownInterface.getSource();
            if (null != dynamicSource && dynamicSource.length > 0) {
                return dynamicSource;
            }
        }
        return new String[]{};
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
        DataValidationHelper helper = sheet.getDataValidationHelper();
        // k 为存在下拉数据集的单元格下表 v为下拉数据集
        map.forEach((index, list) -> {
            // 下拉列表约束数据
            DataValidationConstraint constraint = helper.createExplicitListConstraint(list);
            // 设置下拉单元格的首行 末行 首列 末列
            CellRangeAddressList rangeList = new CellRangeAddressList(1, 65536, index, index);
            // 设置约束
            DataValidation validation = helper.createValidation(constraint, rangeList);
            // 阻止输入非下拉选项的值
            validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            validation.setShowErrorBox(true);
            validation.setSuppressDropDownArrow(true);
            validation.createErrorBox("提示", "此值与单元格定义格式不一致");
            sheet.addValidationData(validation);
        });
    }
}