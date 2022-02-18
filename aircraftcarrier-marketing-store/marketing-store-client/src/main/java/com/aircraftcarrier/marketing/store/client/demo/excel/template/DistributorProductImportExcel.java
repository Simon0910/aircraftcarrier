package com.aircraftcarrier.marketing.store.client.demo.excel.template;

import com.aircraftcarrier.framework.excel.util.ExcelRow;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * DistributorProductImportExcel
 *
 * @author lzp
 * @since 2021-12-10
 */
@Data
public class DistributorProductImportExcel extends ExcelRow {
    /**
     * distributor SpuId
     */
    @ExcelProperty(value = "商品ID")
    private String distributorSpuId;
    /**
     * distributor SkuId
     */
    @ExcelProperty(value = "规格ID(SKUID)")
    private String distributorSkuId;
    /**
     * distributor ShopId
     */
    @ExcelProperty(value = "店铺ID")
    private String distributorShopId;
    /**
     * 供应商 SkuId
     */
    @ExcelProperty(value = "商家编码")
    private String supplierSkuId;

    public String genDistributorKey() {
        return String.format("%s-%s-%s", "DOU_DIAN", distributorShopId, distributorSkuId);
    }

    public String genSupplierKey() {
        return String.format("%s-%s-%s", "POOL", distributorShopId, supplierSkuId);
    }
}
