package com.aircraftcarrier.marketing.store.infrastructure.repository.dataobject;

import com.aircraftcarrier.framework.data.BaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Getter;
import lombok.Setter;


/**
 * <p>
 * 产品表
 * </p>
 *
 * @author lzp
 * @date 2022-06-06
 * @since 1.0
 */
@Getter
@Setter
@TableName("product")
public class ProductDo extends BaseDO<ProductDo> {

    /**
     * 商品编号
     */
    @TableField("goods_no")
    private String goodsNo;

    /**
     * 金额
     */
    @TableField("amount")
    private Integer amount;

    /**
     * 库存
     */
    @TableField("inventory")
    private Integer inventory;

    /**
     * 版本号
     */
    @TableField("version")
    @Version
    private Long version;


}
