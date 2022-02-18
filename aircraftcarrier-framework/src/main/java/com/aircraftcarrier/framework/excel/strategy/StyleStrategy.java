package com.aircraftcarrier.framework.excel.strategy;

import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;

/**
 * @author lzp
 */
public class StyleStrategy {
    private StyleStrategy() {
    }

    public static HorizontalCellStyleStrategy customHorizontalCellStyleStrategy() {
        // 头的策略
        WriteCellStyle headWriteCellStyle = buildHeadCellStyle();
        // 内容的策略
        WriteCellStyle contentWriteCellStyle = buildContentCellStyle();
        // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
        return new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
    }

    /**
     * buildContentCellStyle
     */
    private static WriteCellStyle buildContentCellStyle() {
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        WriteFont contentWriteFont = new WriteFont();
        // 字体大小
        contentWriteFont.setFontName("宋体");
        contentWriteFont.setFontHeightInPoints((short) 12);
        contentWriteCellStyle.setWriteFont(contentWriteFont);
        // 下边框
        contentWriteCellStyle.setBorderBottom(BorderStyle.THIN);
        // 左边框
        contentWriteCellStyle.setBorderLeft(BorderStyle.THIN);
        // 上边框
        contentWriteCellStyle.setBorderTop(BorderStyle.THIN);
        // 右边框
        contentWriteCellStyle.setBorderRight(BorderStyle.THIN);
        // 水平对齐方式
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        // 垂直对齐方式
        contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return contentWriteCellStyle;
    }


    /**
     * buildHeadCellStyle
     */
    private static WriteCellStyle buildHeadCellStyle() {
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        headWriteCellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontHeightInPoints((short) 12);
        headWriteFont.setFontName("微软雅黑");
        headWriteFont.setBold(true);
        headWriteCellStyle.setWriteFont(headWriteFont);
        return headWriteCellStyle;
    }
}
