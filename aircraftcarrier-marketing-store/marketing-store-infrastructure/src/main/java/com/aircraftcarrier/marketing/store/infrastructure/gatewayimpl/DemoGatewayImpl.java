package com.aircraftcarrier.marketing.store.infrastructure.gatewayimpl;

import com.aircraftcarrier.framework.tookit.BeanUtil;
import com.aircraftcarrier.framework.tookit.StringUtil;
import com.aircraftcarrier.marketing.store.domain.gateway.DemoGateway;
import com.aircraftcarrier.marketing.store.domain.model.demo.DemoEntity;
import com.aircraftcarrier.marketing.store.infrastructure.repository.DemoMapper;
import com.aircraftcarrier.marketing.store.infrastructure.repository.dataobject.DemoDo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

/**
 * @author lzp
 */
@Slf4j
@Component
public class DemoGatewayImpl extends ServiceImpl<DemoMapper, DemoDo>
        implements DemoGateway {


    @Autowired
    private DemoMapper demoMapper;

    @Override
    public DemoEntity getEntityById(Serializable id) {
        DemoDo byId = getById(id);
        return BeanUtil.convert(byId, DemoEntity.class);
    }

    @Override
    public List<DemoEntity> selectList(DemoEntity entity) {
        LambdaQueryWrapper<DemoDo> queryWrapper = new LambdaQueryWrapper<DemoDo>()
                .eq(StringUtil.isNotBlank(entity.getSellerNo()), DemoDo::getSellerNo, entity.getSellerNo());
        List<DemoDo> list = demoMapper.selectList(queryWrapper);
        return BeanUtil.convertList(list, DemoEntity.class);
    }
}
