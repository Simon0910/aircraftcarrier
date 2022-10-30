package com.aircraftcarrier.marketing.store.client;

import com.aircraftcarrier.framework.model.BatchResult;
import com.aircraftcarrier.framework.model.response.Page;
import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoCmd;
import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoDeleteCmd;
import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoDetailQryCmd;
import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoPageQryCmd;
import com.aircraftcarrier.marketing.store.client.demo.excel.DemoImportExcelCmd;
import com.aircraftcarrier.marketing.store.client.demo.excel.template.DemoImportExcel;
import com.aircraftcarrier.marketing.store.client.demo.view.DemoPageVo;
import com.aircraftcarrier.marketing.store.client.demo.view.DemoVo;

import java.io.Serializable;
import java.util.List;

/**
 * @author lzp
 */
public interface DemoService {

    /**
     * pageList
     *
     * @param pageQryCmd pageQryCmd
     * @return Page<DemoPageVo>
     */
    Page<DemoPageVo> pageList(DemoPageQryCmd pageQryCmd);

    /**
     * add
     *
     * @param cmd cmd
     * @return int
     */
    int add(DemoCmd cmd);

    /**
     * update
     *
     * @param cmd cmd
     * @return int
     */
    int update(DemoCmd cmd);

    /**
     * getById
     *
     * @param id id
     * @return DemoVo
     */
    DemoVo getById(Serializable id);

    /**
     * selectList
     *
     * @param detailQryCmd detailQryCmd
     * @return List<DemoVo>
     */
    List<DemoVo> selectList(DemoDetailQryCmd detailQryCmd);

    /**
     * delete
     *
     * @param deleteCmd deleteCmd
     * @return boolean
     */
    boolean delete(DemoDeleteCmd deleteCmd);

    /**
     * export
     *
     * @param pageQryCmd pageQryCmd
     * @return List<DemoImportExcel>
     */
    List<DemoImportExcel> export(DemoPageQryCmd pageQryCmd);

    /**
     * importExcel
     *
     * @param demoImportExcelCmd demoImportExcelCmd
     * @return SingleResponse<BatchResult>
     */
    SingleResponse<BatchResult> importExcel(DemoImportExcelCmd demoImportExcelCmd);
}
