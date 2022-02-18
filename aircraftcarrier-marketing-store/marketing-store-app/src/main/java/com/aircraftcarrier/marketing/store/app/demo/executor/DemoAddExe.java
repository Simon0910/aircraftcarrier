package com.aircraftcarrier.marketing.store.app.demo.executor;

import com.aircraftcarrier.framework.tookit.BeanUtils;
import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoCmd;
import com.aircraftcarrier.marketing.store.client.demo.request.DemoAdd;
import com.aircraftcarrier.marketing.store.infrastructure.repository.DemoMapper;
import com.aircraftcarrier.marketing.store.infrastructure.repository.dataobject.DemoDo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author lzp
 */
@Slf4j
@Component
public class DemoAddExe {

    @Resource
    private DemoMapper demoMapper;

    public int execute(DemoCmd cmd) {
        DemoAdd demoAdd = cmd.getDemoAdd();
        DemoDo configDO = BeanUtils.convert(demoAdd, DemoDo.class);
        return demoMapper.insert(configDO);
    }
}
