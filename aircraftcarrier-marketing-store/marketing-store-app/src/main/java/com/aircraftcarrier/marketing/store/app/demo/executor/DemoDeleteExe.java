package com.aircraftcarrier.marketing.store.app.demo.executor;

import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoDeleteCmd;
import com.aircraftcarrier.marketing.store.infrastructure.repository.DemoRepository;
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

    public boolean execute(DemoDeleteCmd deleteCmd) {
        return demoRepository.deleteByIds(deleteCmd.getIds());
    }
}
