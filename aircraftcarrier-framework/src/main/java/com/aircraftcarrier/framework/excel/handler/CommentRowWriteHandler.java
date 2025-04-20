package com.aircraftcarrier.framework.excel.handler;

import com.aircraftcarrier.framework.excel.annotation.ExcelComment;
import com.aircraftcarrier.framework.excel.util.ExcelUtil;
import com.aircraftcarrier.framework.excel.util.Metadata;
import com.aircraftcarrier.framework.tookit.MapUtil;
import com.aircraftcarrier.framework.tookit.StringUtil;
import com.alibaba.excel.write.handler.RowWriteHandler;
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
public class CommentRowWriteHandler implements RowWriteHandler {

    private final Map<Integer, ExcelComment> map;

    public <T> CommentRowWriteHandler(Class<T> templateClass) {
        Field[] fields = templateClass.getDeclaredFields();
        map = MapUtil.newHashMap(fields.length);

        Map<Integer, Metadata> indexNameMap = ExcelUtil.getIndexNameMap(1, templateClass);

        for (Field field : fields) {
            ExcelComment annotation = field.getAnnotation(ExcelComment.class);
            if (null != annotation && StringUtil.isNotBlank(annotation.comment())) {
                indexNameMap.forEach((index, metadata) -> {
                    if (metadata.getField().getName().equals(field.getName())) {
                        map.put(index, annotation);
                    }
                });
            }
        }
    }

    @Override
    public void beforeRowCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Integer rowIndex,
                                Integer relativeRowIndex, Boolean isHead) {
    }

    @Override
    public void afterRowDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row,
                                Integer relativeRowIndex, Boolean isHead) {
        if (Boolean.TRUE.equals(isHead) && !map.isEmpty()) {
            Sheet sheet = writeSheetHolder.getSheet();
            Drawing<?> drawingPatriarch = sheet.createDrawingPatriarch();
            map.forEach((index, excelComment) -> {
                // 批注大小，位置
                XSSFClientAnchor xssfClientAnchor = new XSSFClientAnchor(
                        0, 0, 0, 0,
                        index, row.getRowNum(), index + excelComment.width(), row.getRowNum() + excelComment.height());
                Comment comment = drawingPatriarch.createCellComment(xssfClientAnchor);
                // 输入批注信息
                comment.setString(new XSSFRichTextString(excelComment.comment()));
                // 将批注添加到单元格对象中
                row.getCell(index).setCellComment(comment);
            });
        }
    }

}