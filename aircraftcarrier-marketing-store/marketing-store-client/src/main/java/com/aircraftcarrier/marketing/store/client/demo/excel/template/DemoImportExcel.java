package com.aircraftcarrier.marketing.store.client.demo.excel.template;

import com.aircraftcarrier.framework.enums.DeletedEnum;
import com.aircraftcarrier.framework.excel.annotation.ExcelComment;
import com.aircraftcarrier.framework.excel.annotation.ExcelConvert;
import com.aircraftcarrier.framework.excel.annotation.ExcelDropDown;
import com.aircraftcarrier.framework.excel.convert.IEnumCodeConverter;
import com.aircraftcarrier.framework.excel.convert.IEnumConverter;
import com.aircraftcarrier.framework.excel.util.ExcelRow;
import com.aircraftcarrier.marketing.store.common.enums.DataTypeEnum;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author lzp
 */
@Getter
@Setter
public class DemoImportExcel extends ExcelRow {
    /**
     * id
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 业务主键
     */
    @ExcelProperty(value = "业务主键")
    private String bizNo;

    /**
     * 商家编码
     */
    @ExcelProperty(value = "商家编码")
    private String sellerNo;

    /**
     * 商家名称
     */
    @ExcelProperty(value = "商家名称")
    private String sellerName;

    /**
     * 说明
     */
    @ExcelProperty(value = "说明")
    private String description;

    /**
     * 枚举演示
     */
    @ExcelProperty(value = "枚举演示1", converter = IEnumConverter.class)
    @ExcelDropDown(sourceEnumClass = DataTypeEnum.class)
    @ExcelComment(row = 1, comment = "枚举演示1")
    private DataTypeEnum dataType;

    /**
     * 创建人
     */
    @ExcelProperty(value = "创建人")
    private String createUser;

    /**
     * 修改人
     */
    @ExcelProperty(value = "修改人")
    private String updateUser;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 修改时间
     */
    @ExcelProperty(value = "修改时间")
    private Date updateTime;

    /**
     * 枚举演示
     */
    @ExcelProperty(value = "枚举演示2", converter = IEnumCodeConverter.class)
    @ExcelConvert(sourceEnumClass = DeletedEnum.class)
    @ExcelDropDown(sourceEnumClass = DeletedEnum.class)
    @ExcelComment(row = 1, comment = "枚举演示2")
    private Integer deleted;
}
