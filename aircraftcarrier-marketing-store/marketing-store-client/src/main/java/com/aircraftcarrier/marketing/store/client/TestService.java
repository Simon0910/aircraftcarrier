package com.aircraftcarrier.marketing.store.client;

import java.io.Serializable;
import java.util.Map;

/**
 * @author lzp
 */
public interface TestService {

    /**
     * 测试事务
     */
    void testTransactional();

    /**
     * publishEvent
     */
    void publishEvent();

    /**
     * testLock
     *
     * @param id id
     * @return String
     */
    String testLock(Serializable id);

    /**
     * applyDiscount
     *
     * @param params params
     */
    void applyDiscount(Map<String, Object> params);
}