package com.aircraftcarrier.marketing.store.infrastructure.repository.mybatisplus;

import com.aircraftcarrier.marketing.store.infrastructure.repository.dataobject.DemoDo;
import com.aircraftcarrier.marketing.store.infrastructure.repository.mapper.DemoMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Component;

/**
 * @author lzp
 */
@Component
public class DemoMybatisPlus extends ServiceImpl<DemoMapper, DemoDo> {
}
