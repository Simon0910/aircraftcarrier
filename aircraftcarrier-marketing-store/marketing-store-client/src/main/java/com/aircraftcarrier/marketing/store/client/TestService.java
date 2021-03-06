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
     * testLockKey
     *
     * @param id id
     * @return String
     */
    String testLockKey(Serializable id);

    /**
     * applyDiscount
     *
     * @param params params
     */
    void applyDiscount(Map<String, Object> params);

    /**
     * 并发扣库存防止超卖
     *
     * @param goodsNo 商品编号
     */
    void deductionInventory(Serializable goodsNo);

    /**
     * 多线程测试
     */
    void multiThread();
}