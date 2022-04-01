package com.aircraftcarrier.marketing.store.app.demo.executor;

import com.aircraftcarrier.framework.tookit.BeanUtil;
import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoCmd;
import com.aircraftcarrier.marketing.store.client.demo.request.DemoUpdate;
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
public class DemoUpdateExe {

    @Resource
    private DemoMapper demoMapper;

    public int execute(DemoCmd cmd) {
        DemoUpdate demoUpdate = cmd.getDemoUpdate();
        DemoDo configDO = BeanUtil.convert(demoUpdate, DemoDo.class);
        return demoMapper.updateById(configDO);
    }
}
