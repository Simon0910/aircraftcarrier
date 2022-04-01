package com.aircraftcarrier.marketing.store.app.demo.executor.query;

import com.aircraftcarrier.framework.tookit.BeanUtil;
import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoDetailQryCmd;
import com.aircraftcarrier.marketing.store.domain.gateway.DemoGateway;
import com.aircraftcarrier.marketing.store.domain.model.demo.DemoEntity;
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
    private DemoGateway demoGateway;


    public List<DemoEntity> execute(DemoDetailQryCmd detailQryCmd) {
        DemoEntity entity = BeanUtil.convert(detailQryCmd.getDetailQry(), DemoEntity.class);
        return demoGateway.selectList(entity);
    }
}
