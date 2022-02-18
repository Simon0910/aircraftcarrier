package com.aircraftcarrier.framework.excel.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * SheetInstant
 * 生成多sheet的excel文件
 *
 * @author lzp
 * @version 1.0
 * @date 2020/6/30
 */
@Getter
@Setter
@AllArgsConstructor
public class SheetInstant<T> {
    /**
     * 自定义sheetName
     */
    private String sheetName;
    /**
     * 自定义表头
     */
    private Class<T> headType;
    /**
     * 自定义内容
     */
    private List<T> dataList;
}
