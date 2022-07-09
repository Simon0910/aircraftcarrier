package com.aircraftcarrier.marketing.store.domain;

import com.aircraftcarrier.framework.tookit.ApplicationContextUtil;

/**
 * 获取领域模型工厂
 *
 * @author lzp
 */
public class DomainFactory {

    private DomainFactory() {
    }

    public static <T> T get(Class<T> entityClz) {
        return ApplicationContextUtil.getBean(entityClz);
    }

}
