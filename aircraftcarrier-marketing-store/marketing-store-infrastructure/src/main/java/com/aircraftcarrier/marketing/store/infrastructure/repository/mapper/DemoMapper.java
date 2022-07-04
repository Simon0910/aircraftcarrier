package com.aircraftcarrier.marketing.store.infrastructure.repository.mapper;

import com.aircraftcarrier.framework.data.core.MybatisBaseMapper;
import com.aircraftcarrier.marketing.store.infrastructure.repository.dataobject.DemoDo;

import java.util.List;
import java.util.Map;

/**
 * 审批流配置 Mapper 接口
 *
 * @author lzp
 * @version 1.0
 * @date 2020-11-11
 */
public interface DemoMapper extends MybatisBaseMapper<DemoDo> {

    /**
     * pageList
     *
     * @param pageQry pageQry
     * @return
     */
    List<DemoDo> farmPageList(Map<String, Object> pageQry);

    /**
     * export
     *
     * @return
     */
    List<DemoDo> farmExport();
}
