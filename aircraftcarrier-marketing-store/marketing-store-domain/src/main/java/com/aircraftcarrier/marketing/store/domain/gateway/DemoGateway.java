package com.aircraftcarrier.marketing.store.domain.gateway;


import com.aircraftcarrier.marketing.store.domain.model.demo.DemoEntity;

import java.io.Serializable;
import java.util.List;

/**
 * @author lzp
 */
public interface DemoGateway {

    /**
     * getEntityById
     *
     * @param id id
     * @return
     */
    DemoEntity getEntityById(Serializable id);

    /**
     * selectList
     *
     * @param entity entity
     * @return
     */
    List<DemoEntity> selectList(DemoEntity entity);
}
