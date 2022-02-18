package com.aircraftcarrier.marketing.store.app.demo.executor;

import com.aircraftcarrier.marketing.store.client.demo.cmd.ApprovalDeleteCmd;
import com.aircraftcarrier.marketing.store.infrastructure.repository.DemoMapper;
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
    private DemoMapper demoMapper;

    public int execute(ApprovalDeleteCmd deleteCmd) {
        LambdaUpdateWrapper<DemoDo> condition = new LambdaUpdateWrapper<DemoDo>()
                .in(DemoDo::getId, deleteCmd.getIds());
        return demoMapper.delete(condition);
    }
}
