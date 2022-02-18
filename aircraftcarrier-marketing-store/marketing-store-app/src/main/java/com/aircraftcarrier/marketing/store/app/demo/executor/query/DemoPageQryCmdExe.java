package com.aircraftcarrier.marketing.store.app.demo.executor.query;

import com.aircraftcarrier.framework.data.PageUtil;
import com.aircraftcarrier.framework.model.response.PageResponse;
import com.aircraftcarrier.framework.tookit.ObjUtils;
import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoPageQryCmd;
import com.aircraftcarrier.marketing.store.client.demo.request.DemoPageQry;
import com.aircraftcarrier.marketing.store.infrastructure.repository.DemoMapper;
import com.aircraftcarrier.marketing.store.infrastructure.repository.dataobject.DemoDo;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author lzp
 */
@Component
public class DemoPageQryCmdExe {

    @Resource
    private DemoMapper demoMapper;

    public PageResponse<DemoDo> execute(DemoPageQryCmd pageQryCmd) {
        DemoPageQry pageQry = pageQryCmd.getPageQry();
        return PageUtil.getPage(pageQry, () -> demoMapper.farmPageList(ObjUtils.obj2Map(pageQry)));
    }
}
