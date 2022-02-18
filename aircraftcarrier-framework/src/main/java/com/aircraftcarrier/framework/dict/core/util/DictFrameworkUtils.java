package com.aircraftcarrier.framework.dict.core.util;

import com.aircraftcarrier.framework.dict.core.model.DictData;
import com.aircraftcarrier.framework.dict.core.service.DictDataFrameworkService;
import lombok.extern.slf4j.Slf4j;

/**
 * 字典工具类
 *
 * @author yudao
 */
@Slf4j
public class DictFrameworkUtils {

    private static DictDataFrameworkService service;

    public static void init(DictDataFrameworkService service) {
        DictFrameworkUtils.service = service;
        log.info("[init][初始化 DictFrameworkUtils 成功]");
    }

    public static DictData getDictDataFromCache(String type, String value) {
        return service.getDictDataFromCache(type, value);
    }

    public static DictData parseDictDataFromCache(String type, String label) {
        return service.parseDictDataFromCache(type, label);
    }

}
