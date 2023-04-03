package com.aircraftcarrier.framework.exceltask;

import com.alibaba.excel.annotation.ExcelIgnore;

import java.io.Serializable;

/**
 * @author zhipengliu
 */
public abstract class AbstractUploadData implements Serializable {
    private static final long serialVersionUID = 1L;

    @ExcelIgnore
    private Integer sheetNo;

    @ExcelIgnore
    private Integer rowNo;

    public Integer getSheetNo() {
        return sheetNo;
    }

    public void setSheetNo(Integer sheetNo) {
        this.sheetNo = sheetNo;
    }

    public Integer getRowNo() {
        return rowNo;
    }

    public void setRowNo(Integer rowNo) {
        this.rowNo = rowNo;
    }
}
