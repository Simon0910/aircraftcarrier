package com.aircraftcarrier.marketing.store.app.demo.executor.query;

import com.aircraftcarrier.framework.tookit.BeanUtil;
import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoDetailQryCmd;
import com.aircraftcarrier.marketing.store.domain.model.demo.DemoEntity;
import com.aircraftcarrier.marketing.store.infrastructure.repository.DemoRepository;
import com.aircraftcarrier.marketing.store.infrastructure.repository.dataobject.DemoDo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lzp
 */
@Slf4j
@Component
public class DemoDetailQryCmdExe {

    @Resource
    private DemoRepository demoRepository;

    public List<DemoEntity> execute(DemoDetailQryCmd detailQryCmd) {
        DemoEntity entity = BeanUtil.convert(detailQryCmd.getDetailQry(), DemoEntity.class);
        List<DemoDo> list = demoRepository.selectByDemoEntity(entity);
        return BeanUtil.convertList(list, DemoEntity.class);
    }
}
