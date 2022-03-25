package com.aircraftcarrier.marketing.store.client;

import com.aircraftcarrier.framework.model.response.Page;
import com.aircraftcarrier.marketing.store.client.demo.cmd.ApprovalDeleteCmd;
import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoCmd;
import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoDetailQryCmd;
import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoPageQryCmd;
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
     * @return
     */
    Page<DemoPageVo> pageList(DemoPageQryCmd pageQryCmd);

    /**
     * add
     *
     * @param cmd
     * @return
     */
    int add(DemoCmd cmd);

    /**
     * update
     *
     * @param cmd
     * @return
     */
    int update(DemoCmd cmd);

    /**
     * getById
     *
     * @param id id
     * @return
     */
    DemoVo getById(Serializable id);

    /**
     * selectList
     *
     * @param detailQryCmd detailQryCmd
     * @return
     */
    List<DemoVo> selectList(DemoDetailQryCmd detailQryCmd);

    /**
     * delete
     *
     * @param deleteCmd deleteCmd
     * @return
     */
    int delete(ApprovalDeleteCmd deleteCmd);

    /**
     * export
     *
     * @param pageQryCmd pageQryCmd
     * @return
     */
    List<DemoImportExcel> export(DemoPageQryCmd pageQryCmd);

}
