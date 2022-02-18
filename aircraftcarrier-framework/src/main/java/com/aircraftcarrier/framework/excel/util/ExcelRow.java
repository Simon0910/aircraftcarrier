package com.aircraftcarrier.framework.excel.util;

import com.alibaba.excel.annotation.ExcelIgnore;

import java.io.Serializable;

/**
 * @author lzp
 */
public class ExcelRow implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;
    /**
     * rowNo
     */
    @ExcelIgnore
    protected Integer rowNo;

    public Integer getRowNo() {
        return rowNo;
    }

    public void setRowNo(Integer rowNo) {
        this.rowNo = rowNo;
    }
}
