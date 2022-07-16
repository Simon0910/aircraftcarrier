package com.aircraftcarrier.marketing.store.app.demo.executor.excel;

import com.aircraftcarrier.framework.data.MybatisBatchUtil;
import com.aircraftcarrier.framework.tookit.BeanUtil;
import com.aircraftcarrier.marketing.store.app.demo.convert.DemoDoToDemoImportExcelConvert;
import com.aircraftcarrier.marketing.store.app.demo.convert.DemoDoToImportExcelConvert;
import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoPageQryCmd;
import com.aircraftcarrier.marketing.store.client.demo.excel.template.DemoImportExcel;
import com.aircraftcarrier.marketing.store.infrastructure.repository.dataobject.DemoDo;
import com.aircraftcarrier.marketing.store.infrastructure.repository.mapper.DemoMapper;
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

        List<DemoImportExcel> excels = DemoDoToDemoImportExcelConvert.INSTANCE.convertList(list);
        return BeanUtil.convertList(list, DemoImportExcel.class, DemoDoToImportExcelConvert::convertDifference);
    }
}
