package com.aircraftcarrier.marketing.store.infrastructure.repository.mapper;

import com.aircraftcarrier.framework.data.core.MybatisBaseMapper;
import com.aircraftcarrier.framework.model.request.PageQuery;
import com.aircraftcarrier.framework.model.request.Query;
import com.aircraftcarrier.marketing.store.infrastructure.repository.dataobject.ProductDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 产品表 Mapper
 *
 * @author lzp
 * @date 2022-06-06
 * @since 1.0
 */
@Mapper
public interface ProductMapper extends MybatisBaseMapper<ProductDo> {

    /**
     * 分页查询
     *
     * @param pageQry 分页查询参数
     * @return {@link List<ProductDo>}
     */
    List<ProductDo> listPage(PageQuery pageQry);

    /**
     * 批量导出
     *
     * @param exportQry 导出参数
     * @return {@link List<ProductDo>}
     */
    List<ProductDo> excelExport(Query exportQry);

    /**
     * 更新库存 By Version
     *
     * @param id              主键
     * @param version         版本号
     * @param appendInventory 追加数
     * @return int
     */
    int updateInventoryByVersion(@Param("id") Long id, @Param("version") Long version, @Param("appendInventory") Integer appendInventory);

    /**
     * 更新库存
     *
     * @param id              主键
     * @param appendInventory 追加数
     * @return int
     */
    int updateInventoryDirect(@Param("id") Long id, @Param("appendInventory") Integer appendInventory);
}
