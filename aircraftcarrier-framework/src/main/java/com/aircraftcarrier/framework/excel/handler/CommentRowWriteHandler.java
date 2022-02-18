package com.aircraftcarrier.framework.excel.handler;

import com.aircraftcarrier.framework.excel.annotation.ExcelComment;
import com.aircraftcarrier.framework.tookit.MapUtil;
import com.aircraftcarrier.framework.tookit.StringUtils;
import com.alibaba.excel.write.handler.AbstractRowWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * 自定义拦截器.新增注释,第一行头加批注
 *
 * @author Jiaju Zhuang
 */
public class CommentRowWriteHandler extends AbstractRowWriteHandler {

    private final Map<Integer, ExcelComment> map;

    public <T> CommentRowWriteHandler(Class<T> templateClass) {
        Field[] fields = templateClass.getDeclaredFields();
        map = MapUtil.newHashMap(fields.length);
        for (int i = 0, len = fields.length; i < len; i++) {
            Field field = fields[i];
            ExcelComment annotation = field.getAnnotation(ExcelComment.class);
            if (null != annotation && StringUtils.isNotBlank(annotation.comment())) {
                map.put(annotation.index() != -1 ? annotation.index() : i, annotation);
            }
        }
    }

    @Override
    public void afterRowDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row,
                                Integer relativeRowIndex, Boolean isHead) {
        if (Boolean.TRUE.equals(isHead) && !map.isEmpty()) {
            Sheet sheet = writeSheetHolder.getSheet();
            Drawing<?> drawingPatriarch = sheet.createDrawingPatriarch();
            map.forEach((index, excelComment) -> {
                if (row.getRowNum() != excelComment.row()) {
                    return;
                }
                // 在第一行 第二列创建一个批注
                XSSFClientAnchor xssfClientAnchor = new XSSFClientAnchor(
                        0, 0, 0, 0,
                        index,
                        relativeRowIndex,
                        excelComment.width(),
                        excelComment.height());
                Comment comment = drawingPatriarch.createCellComment(xssfClientAnchor);
                // 输入批注信息
                comment.setString(new XSSFRichTextString(excelComment.comment()));
                // 将批注添加到单元格对象中
                sheet.getRow(0).getCell(1).setCellComment(comment);
            });
        }
    }

}