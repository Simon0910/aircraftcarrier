package com.aircraftcarrier.marketing.store.client.demo.excel.template;

import com.aircraftcarrier.framework.enums.DeletedEnum;
import com.aircraftcarrier.framework.excel.annotation.ExcelComment;
import com.aircraftcarrier.framework.excel.annotation.ExcelDropDown;
import com.aircraftcarrier.framework.excel.annotation.convert.ExcelIEnumCodeConvert;
import com.aircraftcarrier.framework.excel.annotation.valid.ExcelBigDecimal;
import com.aircraftcarrier.framework.excel.annotation.valid.ExcelNumber;
import com.aircraftcarrier.framework.excel.convert.IEnumCodeConverter;
import com.aircraftcarrier.framework.excel.convert.IEnumConverter;
import com.aircraftcarrier.framework.excel.util.ExcelRow;
import com.aircraftcarrier.marketing.store.client.demo.excel.suport.UnitDropDown;
import com.aircraftcarrier.marketing.store.common.enums.DataTypeEnum;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.format.NumberFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author lzp
 */
@Getter
@Setter
public class DemoImportExcel extends ExcelRow {

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
    @ExcelProperty(value = {"说明"})
    private String description;

    /**
     * 枚举演示
     */
    @ExcelProperty(value = "枚举演示1", converter = IEnumConverter.class)
    @ExcelDropDown(sourceEnumClass = DataTypeEnum.class)
    @ExcelComment(width = 1, height = 10, comment = "枚举演示1")
    private DataTypeEnum dataType;

    /**
     * 金额
     */
    @ExcelProperty(value = "金额")
    @NumberFormat
    @NotNull(message = "金额参数必传")
    @Digits(integer = 6, fraction = 2, message = "金额不符合decimal(8,2)格式")
    @DecimalMin(value = "0", message = "金额最小不能小于0")
    @DecimalMax(value = "999999.99", message = "金额最大不能大于999999.99")
    @ExcelBigDecimal(min = "0", max = "999999.99", integer = 6, scale = 2)
    private BigDecimal amount;

    /**
     * 日期
     */
    @ExcelProperty(value = "日期")
    private Date dateTime;


    /**
     * 创建人
     */
    @ExcelProperty(value = "创建人")
    private String createUser;

    /**
     * 修改人
     */
    @ExcelProperty(value = {"修改人"})
    private String updateUser;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 修改时间
     */
    @ExcelProperty(value = "修改时间")
//    @ExcelProperty(value = "", index = 11)
//    @ExcelProperty(value = " ", index = 11)
    private Date updateTime;

    /**
     * 枚举演示
     */
    @ExcelProperty(value = "枚举演示2", converter = IEnumCodeConverter.class, order = 3)
    @ExcelIEnumCodeConvert(sourceEnumClass = DeletedEnum.class)
    @ExcelDropDown(sourceEnumClass = DeletedEnum.class)
    @ExcelComment(width = 10, height = 1, comment = "枚举演示2")
    private Integer deleted;

    /**
     * 宽度
     */
    @ApiModelProperty(value = "宽度", required = true)
    @NotNull(message = "宽度参数必传")
    @ExcelNumber(formula1 = 0, formula2 = 200)
    private Integer weight;


    @ExcelProperty(value = "单位")
    @ExcelDropDown(sourceClass = UnitDropDown.class)
    private String unit;
}
