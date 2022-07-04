package com.aircraftcarrier.marketing.store.app.demo.executor.excel;

import com.aircraftcarrier.framework.data.MybatisBatchUtil;
import com.aircraftcarrier.marketing.store.app.demo.convert.DemoDo2DemoImportExcelConvert;
import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoPageQryCmd;
import com.aircraftcarrier.marketing.store.client.demo.excel.template.DemoImportExcel;
import com.aircraftcarrier.marketing.store.infrastructure.repository.mapper.DemoMapper;
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
public class DemoExportExe {

    @Resource
    private DemoMapper demoMapper;

    public List<DemoImportExcel> execute(DemoPageQryCmd pageQryCmd) {

        List<DemoDo> list = MybatisBatchUtil.selectAllListBatchWithId(() -> demoMapper.farmExport());

        return DemoDo2DemoImportExcelConvert.INSTANCE.convertList(list);
    }
}
