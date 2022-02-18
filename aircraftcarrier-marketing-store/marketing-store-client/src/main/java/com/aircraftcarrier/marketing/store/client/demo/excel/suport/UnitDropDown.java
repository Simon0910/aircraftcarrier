package com.aircraftcarrier.marketing.store.client.demo.excel.suport;

import com.aircraftcarrier.framework.excel.handler.DropDownInterface;

/**
 * @author lzp
 */
public class UnitDropDown implements DropDownInterface {
    @Override
    public String[] getSource() {
        // 数据库查询
        return new String[]{"g", "kg", "t", "ml", "l", "米", "千米"};
    }
}