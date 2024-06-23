package com.aircraftcarrier.marketing.store.adapter.exceltask.task01;

import com.aircraftcarrier.framework.exceltask.AbstractExcelRow;
import com.alibaba.excel.annotation.ExcelProperty;

/**
 * @author zhipengliu
 */
public class MyExcelRow extends AbstractExcelRow {

    @ExcelProperty(value = "款号")
    private String styleCode;

    @ExcelProperty(value = "尺码")
    private String size;

    @ExcelProperty(value = "商品类别")
    private String type;

    public String getStyleCode() {
        return styleCode;
    }

    public void setStyleCode(String styleCode) {
        this.styleCode = styleCode;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
