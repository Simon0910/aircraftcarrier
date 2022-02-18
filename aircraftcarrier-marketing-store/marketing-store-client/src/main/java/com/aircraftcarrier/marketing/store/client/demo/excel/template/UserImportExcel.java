package com.aircraftcarrier.marketing.store.client.demo.excel.template;

import com.aircraftcarrier.framework.excel.annotation.ExcelComment;
import com.aircraftcarrier.framework.excel.annotation.ExcelDropDown;
import com.aircraftcarrier.marketing.store.client.demo.excel.suport.UnitDropDown;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lzp
 */
@Getter
@Setter
public class UserImportExcel {

    @ExcelProperty(value = {"title", "商品名称"})
    @ExcelComment(row = 1, comment = "商品名称")
    private String name;

    @ExcelProperty(value = {"title", "商品类型"})
    @ExcelDropDown(source = {"固体", "液体"})
    @ExcelComment(row = 1, comment = "商品类型")
    private String type;

    @ExcelProperty(value = "单位")
    @ExcelDropDown(sourceClass = UnitDropDown.class)
    @ExcelComment(width = 10, height = 10, comment = "单位")
    private String unit;
}
