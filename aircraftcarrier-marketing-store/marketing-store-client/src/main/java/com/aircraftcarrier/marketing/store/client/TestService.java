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

    /**
     * redis decrBy
     *
     * @param goodsNo 商品编号
     */
    void decrBy(String goodsNo);

    /**
     * 递归事务测试
     *
     * @param str str
     * @param i   i
     */
    void recursionTransactional(String str, int i);

    /**
     * 递归事务测试2
     *
     * @param str str
     * @param i   i
     */
    void recursionTransactional2(String str, int i);

    /**
     * 可重入锁
     *
     * @param key key
     */
    void reentrantLock(String key);
}