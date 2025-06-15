package com.aircraftcarrier.marketing.store.client.demo.excel.suport;

import com.aircraftcarrier.framework.excel.handler.DropDownInterface;
import com.aircraftcarrier.marketing.store.client.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author lzp
 */
@Slf4j
@Component(value = "unitDropDown")
public class UnitDropDown implements DropDownInterface {

    @Resource
    CommonService commonService;

    @Override
    public String[] getSource() {
        log.info("查询数据字典");
        // 数据库查询
        return new String[]{"g", "kg", "t", "ml", "l", "米", "千米"};
    }
}