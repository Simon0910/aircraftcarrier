package com.aircraftcarrier.marketing.store.app.demo.executor.query;

import com.aircraftcarrier.framework.data.PageUtil;
import com.aircraftcarrier.framework.model.response.Page;
import com.aircraftcarrier.framework.tookit.BeanMapUtil;
import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoPageQryCmd;
import com.aircraftcarrier.marketing.store.client.demo.request.DemoPageQry;
import com.aircraftcarrier.marketing.store.infrastructure.repository.dataobject.DemoDo;
import com.aircraftcarrier.marketing.store.infrastructure.repository.mapper.DemoMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author lzp
 */
@Component
public class DemoPageQryCmdExe {

    @Resource
    private DemoMapper demoMapper;

    public Page<DemoDo> execute(DemoPageQryCmd pageQryCmd) {
        DemoPageQry pageQry = pageQryCmd.getPageQry();
        return PageUtil.getPage(pageQry, () -> demoMapper.farmPageList(BeanMapUtil.obj2Map(pageQry)));
    }
}
