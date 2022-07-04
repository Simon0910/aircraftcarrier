package com.aircraftcarrier.marketing.store.app.demo.executor;

import com.aircraftcarrier.marketing.store.client.demo.cmd.ApprovalDeleteCmd;
import com.aircraftcarrier.marketing.store.infrastructure.repository.DemoRepository;
import com.aircraftcarrier.marketing.store.infrastructure.repository.mapper.DemoMapper;
import com.aircraftcarrier.marketing.store.infrastructure.repository.dataobject.DemoDo;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author lzp
 */
@Slf4j
@Component
public class DemoDeleteExe {

    @Resource
    private DemoRepository demoRepository;

    public boolean execute(ApprovalDeleteCmd deleteCmd) {
        return demoRepository.deleteByIds(deleteCmd.getIds());
    }
}
