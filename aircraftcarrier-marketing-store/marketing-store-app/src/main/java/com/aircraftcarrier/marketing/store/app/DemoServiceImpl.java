package com.aircraftcarrier.marketing.store.app;

import com.aircraftcarrier.framework.model.BatchResult;
import com.aircraftcarrier.framework.model.response.Page;
import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.framework.tookit.BeanUtil;
import com.aircraftcarrier.marketing.store.app.demo.executor.DemoAddExe;
import com.aircraftcarrier.marketing.store.app.demo.executor.DemoDeleteExe;
import com.aircraftcarrier.marketing.store.app.demo.executor.DemoUpdateExe;
import com.aircraftcarrier.marketing.store.app.demo.executor.excel.DemoExportExe;
import com.aircraftcarrier.marketing.store.app.demo.executor.excel.DemoImportCmdExe;
import com.aircraftcarrier.marketing.store.app.demo.executor.query.DemoDetailQryCmdExe;
import com.aircraftcarrier.marketing.store.app.demo.executor.query.DemoPageQryCmdExe;
import com.aircraftcarrier.marketing.store.client.DemoService;
import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoCmd;
import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoDeleteCmd;
import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoDetailQryCmd;
import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoPageQryCmd;
import com.aircraftcarrier.marketing.store.client.demo.excel.DemoImportExcelCmd;
import com.aircraftcarrier.marketing.store.client.demo.excel.template.DemoImportExcel;
import com.aircraftcarrier.marketing.store.client.demo.view.DemoPageVo;
import com.aircraftcarrier.marketing.store.client.demo.view.DemoVo;
import com.aircraftcarrier.marketing.store.domain.model.demo.DemoEntity;
import com.aircraftcarrier.marketing.store.infrastructure.repository.DemoRepository;
import com.aircraftcarrier.marketing.store.infrastructure.repository.dataobject.DemoDo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * @author lzp
 */
@Slf4j
@Service
public class DemoServiceImpl implements DemoService {

    @Resource
    private DemoRepository demoRepository;
    @Resource
    private DemoPageQryCmdExe demoPageQryCmdExe;
    @Resource
    private DemoDetailQryCmdExe demoDetailQryCmdExe;
    @Resource
    private DemoExportExe demoExportExe;
    @Resource
    private DemoDeleteExe demoDeleteExe;
    @Resource
    private DemoAddExe demoAddExe;
    @Resource
    private DemoUpdateExe demoUpdateExe;
    @Resource
    private DemoImportCmdExe demoImportCmdExe;

    @Override
    public Page<DemoPageVo> pageList(DemoPageQryCmd pageQryCmd) {
        Page<DemoDo> page = demoPageQryCmdExe.execute(pageQryCmd);
        return Page.build(page.getList(), page.getTotal(), DemoPageVo.class);
    }

    @Override
    public int add(DemoCmd cmd) {
        return demoAddExe.execute(cmd);
    }

    @Override
    public int update(DemoCmd cmd) {
        return demoUpdateExe.execute(cmd);
    }

    @Override
    public DemoVo getById(Serializable id) {
        DemoDo demoDo = demoRepository.getById(id);
        return BeanUtil.convert(demoDo, DemoVo.class);
    }

    @Override
    public List<DemoVo> selectList(DemoDetailQryCmd detailQryCmd) {
        List<DemoEntity> entityList = demoDetailQryCmdExe.execute(detailQryCmd);
        return BeanUtil.convertList(entityList, DemoVo.class);
    }

    @Override
    public boolean delete(DemoDeleteCmd deleteCmd) {
        return demoDeleteExe.execute(deleteCmd);
    }

    @Override
    public List<DemoImportExcel> export(DemoPageQryCmd pageQryCmd) {
        return demoExportExe.execute(pageQryCmd);
    }

    @Override
    public SingleResponse<BatchResult> importExcel(DemoImportExcelCmd demoImportExcelCmd) {
        return demoImportCmdExe.execute(demoImportExcelCmd);
    }


}
