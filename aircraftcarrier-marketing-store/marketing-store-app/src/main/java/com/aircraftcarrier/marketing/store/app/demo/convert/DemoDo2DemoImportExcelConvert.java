package com.aircraftcarrier.marketing.store.app.demo.convert;

import cn.hutool.core.collection.CollUtil;
import com.aircraftcarrier.marketing.store.client.demo.excel.template.DemoImportExcel;
import com.aircraftcarrier.marketing.store.common.enums.DataTypeEnum;
import com.aircraftcarrier.marketing.store.infrastructure.repository.dataobject.DemoDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

/**
 * mapstruct 性能最好
 *
 * @author lzp
 */
@Mapper
public interface DemoDo2DemoImportExcelConvert {
    /**
     * INSTANCE
     */
    DemoDo2DemoImportExcelConvert INSTANCE = Mappers.getMapper(DemoDo2DemoImportExcelConvert.class);

    /**
     * 字段相同，但是含义不同，忽略
     *
     * @param bean bean
     * @return LoginUser
     */
    @Mapping(source = "updateTime", target = "updateTime", ignore = true)
    DemoImportExcel convert0(DemoDo bean);

    /**
     * convert
     * 目的，为了设置 UserTypeEnum.ADMIN.getValue()
     *
     * @param bean bean
     * @return LoginUser
     */
    default DemoImportExcel convert(DemoDo bean) {
        DemoImportExcel importExcel = convert0(bean);
        if (importExcel == null) {
            return null;
        }
        return importExcel;
    }

    /**
     * 转换集合
     *
     * @param sourceList sourceList
     * @return List<DemoImportExcel>
     */
    default List<DemoImportExcel> convertList(List<DemoDo> sourceList) {
        if (CollUtil.isEmpty(sourceList)) {
            return new ArrayList<>();
        }
        List<DemoImportExcel> targetList = new ArrayList<>(sourceList.size());
        for (DemoDo demoDo : sourceList) {
            targetList.add(convert(demoDo));
        }
        return targetList;
    }
}
