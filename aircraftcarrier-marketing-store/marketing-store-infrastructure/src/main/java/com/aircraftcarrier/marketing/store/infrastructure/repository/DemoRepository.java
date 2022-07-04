package com.aircraftcarrier.marketing.store.infrastructure.repository;

import cn.hutool.core.collection.CollUtil;
import com.aircraftcarrier.framework.exception.SysException;
import com.aircraftcarrier.framework.tookit.StringUtil;
import com.aircraftcarrier.marketing.store.domain.model.demo.DemoEntity;
import com.aircraftcarrier.marketing.store.infrastructure.repository.dataobject.DemoDo;
import com.aircraftcarrier.marketing.store.infrastructure.repository.mapper.DemoMapper;
import com.aircraftcarrier.marketing.store.infrastructure.repository.mybatisplus.DemoMybatisPlus;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * @author lzp
 * 各种XxxWrapper, 封装在Repository中
 */
@Repository
public class DemoRepository {

    /**
     * DemoMapper
     */
    @Resource
    DemoMapper demoMapper;

    /**
     * DemoMybatisPlus
     */
    @Resource
    DemoMybatisPlus demoMybatisPlus;

    /**
     * selectByDemoEntity
     *
     * @param entity entity
     * @return List<DemoDo>
     */
    public List<DemoDo> selectByDemoEntity(DemoEntity entity) {
        return new LambdaQueryChainWrapper<>(demoMapper)
                .ge(StringUtil.isNotBlank(entity.getSellerNo()), DemoDo::getSellerNo, entity.getSellerNo())
                .list();
    }

    /**
     * deleteByIds
     *
     * @param ids ids
     * @return int
     */
    public boolean deleteByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw new SysException("ids must not be empty");
        }
        return demoMybatisPlus.removeBatchByIds(ids);
    }

    /**
     * getById
     *
     * @param id id
     * @return DemoDo
     */
    public DemoDo getById(Serializable id) {
        if (id == null) {
            throw new SysException("id must not be null");
        }
        return demoMybatisPlus.getById(id);
    }
}
