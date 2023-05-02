package com.aircraftcarrier.marketing.store.infrastructure.gateway;

import com.aircraftcarrier.framework.exception.BizException;
import com.aircraftcarrier.framework.exception.SysException;
import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.framework.tookit.LockKeyUtil;
import com.aircraftcarrier.marketing.store.domain.gateway.ProductGateway;
import com.aircraftcarrier.marketing.store.infrastructure.repository.dataobject.ProductDo;
import com.aircraftcarrier.marketing.store.infrastructure.repository.mapper.ProductMapper;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author lzp
 */
@Slf4j
@Component
public class ProductGatewayImpl implements ProductGateway {
    /**
     * 库存标记 (集群使用redis)
     * expireAfterAccess 在访问之后指定多少秒过期
     * expireAfterWrite 在写入数据后指定多少秒过期
     * expireAfter 通过重写Expire接口，指定过期时间
     */
    public static final Cache<String, Integer> ZERO_STOCK_CACHE = Caffeine.newBuilder()
            // 在写入数据后指定多少秒过期
            .expireAfterWrite(10, TimeUnit.SECONDS)
            // 初始的缓存空间大小
            .initialCapacity(16)
            // 缓存的最大条数
            .maximumSize(32).build();

    /**
     * 商品缓存 (集群使用redis)
     * expireAfterAccess 在访问之后指定多少秒过期
     * expireAfterWrite 在写入数据后指定多少秒过期
     * expireAfter 通过重写Expire接口，指定过期时间
     */
    public static final Cache<String, ProductDo> STOCK_CACHE = Caffeine.newBuilder()
            // 在写入数据后指定多少秒过期
            .expireAfterWrite(10, TimeUnit.SECONDS)
            // 初始的缓存空间大小
            .initialCapacity(16)
            // 缓存的最大条数
            .maximumSize(32).build();

    @Resource
    private ProductMapper productMapper;

    @Override
    public SingleResponse<Void> addInventory(Serializable goodsNo, Integer addNum) {
        if (addNum < 1) {
            throw new SysException("addInventory must be more than 0");
        }
        updateInventory(goodsNo, addNum);
        ZERO_STOCK_CACHE.invalidate(goodsNo);
        return SingleResponse.ok();
    }

    @Override
    public SingleResponse<Void> deductionInventory(Serializable goodsNo) {
        return deductionInventory(goodsNo, 1);
    }

    @Override
    public SingleResponse<Void> deductionInventory(Serializable goodsNo, Integer deductionNum) {
        if (deductionNum < 1) {
            throw new SysException("deductionNum must be more than 0");
        }
        return updateInventory(goodsNo, -deductionNum);
    }

    private SingleResponse<Void> updateInventory(Serializable goodsNo, Integer appendInventory) {
        if (appendInventory < 1) {
            Integer stock = ZERO_STOCK_CACHE.getIfPresent(String.valueOf(goodsNo));
            if (stock != null) {
                log.error("库存不足了哦-0");
                return buildErrorSingleResponse("库存不足了哦");
            }
        }
//        ProductDo productDo = getProductDoFromLocalCache(String.valueOf(goodsNo));
//        // updateInventoryByVersion 主要使用 getProductDoFromLocalCache 缓存的主键,
//        // 和缓存的其他数据(缓存10秒的库存有点作用判断库存不足，缓存10秒的版本号可能会导致第一个请求也要重试，但是本来99%的都要重试，增加第一个请求重试的代价远小于获取主键的收益更大)关系不大, 不影响程序执行的正确性
//        return updateInventoryByVersion(productDo, productDo.getVersion(), productDo.getInventory(), appendInventory);
        ProductDo productDo = getProductDoFromLocalCache(String.valueOf(goodsNo));
        // updateInventory 主要使用 getProductDoFromLocalCache 缓存的主键,
        // 和缓存的其他数据(缓存10秒的库存有点作用判断库存不足，缓存10秒的版本号没有一点关系)关系不大, 不影响程序执行的正确性
        return updateInventory(productDo, productDo.getInventory(), appendInventory);
    }

    private SingleResponse<Void> buildErrorSingleResponse(String msg) {
        return SingleResponse.error(500, msg);
    }

    public ProductDo getProductDo(Serializable goodsNo) {
        List<ProductDo> list = new LambdaQueryChainWrapper<>(productMapper)
                .eq(ProductDo::getGoodsNo, goodsNo)
                .list();
        if (list.isEmpty()) {
            log.error("商品不存在");
            markNotFound(goodsNo);
            throw new BizException("商品不存在");
        }
        if (list.size() != 1) {
            // 暴漏数据异常，此问题需要从源头解决！！！
            markNotFound(goodsNo);
            throw new BizException("Duplicate keys result in objects that cannot be mapped");
        }

        ProductDo productDo = list.get(0);
        log.info("getProductDo: {}", JSON.toJSONString(productDo));
        if (productDo.getInventory() < 1) {
            markNoStock(goodsNo);
        }
        return productDo;
    }

    /**
     * 获取 (主键, 缓存10秒的库存，缓存10秒的版本号)
     * <p>
     * 库存和版本号 和数据库不是强一致性, 有可能更新缓存后，数据库立马又变化了
     */
    private ProductDo getProductDoFromLocalCache(Serializable goodsNo) {
        ProductDo cacheProduct = STOCK_CACHE.getIfPresent(goodsNo);
        if (cacheProduct == null) {
            cacheProduct = getProductDo(goodsNo);
            STOCK_CACHE.put(String.valueOf(goodsNo), cacheProduct);
        }
        return cacheProduct;
    }

    /**
     * 标记不存在，防止穿透
     */
    private void markNotFound(Serializable goodsNo) {
        markNoStock(goodsNo);
    }

    /**
     * 标记无库存
     */
    private void markNoStock(Serializable goodsNo) {
        ZERO_STOCK_CACHE.put(String.valueOf(goodsNo), 0);
    }

    /**
     * 更新缓存 缓存10秒的库存，缓存10秒的版本号
     * <p>
     * 库存和版本号 和数据库不是强一致性, 有可能更新缓存后，数据库立马又变化了
     */
    private void updateProductCache(ProductDo productDo) {
        STOCK_CACHE.put(String.valueOf(productDo.getGoodsNo()), productDo);
    }

    /**
     * 更新库存1
     * 与用户侧 与 运营侧无关，底层基础设施通用方法
     * 本地单机测试：
     * 1000 人抢 1000个库存 5041ms （LambdaQueryChainWrapper查询耗时 + update耗时）
     * 1000 人抢 0个库存 384ms （LambdaQueryChainWrapper查询耗时）
     *
     * @param productDo       数据主键 （非业务主键）
     * @param version         数据更新版本号
     * @param originInventory 原始库存
     * @param appendInventory 追加扣减库存（正负代表加减库存）
     * @return SingleResponse
     */
    private SingleResponse<Void> updateInventoryByVersion(ProductDo productDo, Long version, Integer originInventory, Integer appendInventory) {
        if (appendInventory == 0) {
            // 避免无效递增版本号，无需不发送MQ库存变更通知，若更新所有字段和数据库相同避免死循环
            log.warn("库存无变化");
            return buildErrorSingleResponse("库存无变化");
        }

        if (appendInventory < 1) {
            Integer stock = ZERO_STOCK_CACHE.getIfPresent(String.valueOf(productDo.getGoodsNo()));
            if (stock != null) {
                log.error("库存不足了哦-1");
                return buildErrorSingleResponse("库存不足了哦");
            }
        }

        int newInventory = originInventory + appendInventory;
        if (newInventory < 0) {
            log.error("库存不足");
            return buildErrorSingleResponse("库存不足");
        }

        Long id = productDo.getId();
        int updatedNum = productMapper.updateInventoryByVersion(id, version, appendInventory);
        if (updatedNum < 1) {
            // 同一条记录当版本变化 才会走到这里
            LockKeyUtil.lock(id.toString());

            try {
                if (appendInventory < 1) {
                    Integer stock = ZERO_STOCK_CACHE.getIfPresent(String.valueOf(productDo.getGoodsNo()));
                    if (stock != null) {
                        log.error("库存不足了哦-3");
                        return buildErrorSingleResponse("库存不足了哦");
                    }
                }

                // 版本变化了，需要查询实时库存
                ProductDo newProductDo = productMapper.selectById(id);
                if (newProductDo == null) {
                    log.error("商品不存在...");
                    return buildErrorSingleResponse("商品不存在...");
                }
                if (newProductDo.getInventory() < 1) {
                    markNoStock(newProductDo.getGoodsNo());
                }
                if ((newProductDo.getInventory() + appendInventory) < 0) {
                    log.error("库存不足...");
                    return buildErrorSingleResponse("库存不足...");
                }
                log.info("retry..." + id);
                updateInventoryByVersion(newProductDo, newProductDo.getVersion(), newProductDo.getInventory(), appendInventory);

            } finally {
                LockKeyUtil.unlock(id.toString());
            }
        }

        updateProductCache(productDo);
        return SingleResponse.ok();
    }

    /**
     * 更新库存2
     * 本地单机测试：
     * 1000 人抢 1000个库存 1280ms （LambdaQueryChainWrapper查询耗时 + update耗时）
     * 1000 人抢 0个库存 338ms（LambdaQueryChainWrapper查询耗时）
     * 更新库存2 比 更新库存1 快5倍 why？
     * 原因：
     * 方法1, 有5次应用内依次执行成功，大量请求获取行锁后执行失败后重返回到应用内排队串行执行重试， 995人应用内排队串行执行抢成功，
     * 注：为什么要返回应用内排队呢？version版本号变化了，所有要获取新的版本号重新执行（版本号乐观锁不适用于大量争抢例如秒杀的场景，适用于后端运营管理系统同时操作一个页面）
     * 方法2, 全部仅仅在mysql层行锁上排队串行执行，返回到应用内排队0人
     * <p>
     * 当增加到 10000 人抢 10000 个库存 方法2也不行了，
     * 原因：因为大量的请求导致mysql的io飙升， 且在行锁上排队串行执行导致
     *
     * @param productDo       数据主键 （非业务主键）
     * @param originInventory 原始库存
     * @param appendInventory 追加扣减库存（正负代表加减库存）
     * @return SingleResponse
     */
    private SingleResponse<Void> updateInventory(ProductDo productDo, Integer originInventory, Integer appendInventory) {
        if (appendInventory == 0) {
            // 避免无效递增版本号，无需不发送MQ库存变更通知，若更新所有字段和数据库相同避免死循环
            log.warn("库存无变化");
            return buildErrorSingleResponse("库存无变化");
        }

        if (appendInventory < 1) {
            Integer stock = ZERO_STOCK_CACHE.getIfPresent(String.valueOf(productDo.getGoodsNo()));
            if (stock != null) {
                log.error("库存不足了哦-1");
                return buildErrorSingleResponse("库存不足了哦");
            }
        }

        int newInventory = originInventory + appendInventory;
        if (newInventory < 0) {
            log.error("库存不足");
            return buildErrorSingleResponse("库存不足");
        }

        Long id = productDo.getId();
        int updatedNum = productMapper.updateInventoryDirect(id, appendInventory);
        if (updatedNum < 1) {
            // 当库存真的不足 才会走到这里
            // 模拟又添加库存
//            boolean rr = RequestLimitUtil.getInstance().require("rr", 1);
//            if (rr) {
//                ThreadPoolUtil.executeVoid(() -> addInventory(String.valueOf(productDo.getGoodsNo()), 5));
//            }

            if (appendInventory < 1) {
                Integer stock = ZERO_STOCK_CACHE.getIfPresent(String.valueOf(productDo.getGoodsNo()));
                if (stock != null) {
                    // 防止进入synchronized
                    log.error("库存不足了哦-2");
                    return buildErrorSingleResponse("库存不足了哦");
                }
            }

            // 防止无库存并发mysql的IO飙升
            synchronized (id.toString().intern()) {
                if (appendInventory < 1) {
                    Integer stock = ZERO_STOCK_CACHE.getIfPresent(String.valueOf(productDo.getGoodsNo()));
                    if (stock != null) {
                        log.error("库存不足了哦-3");
                        return buildErrorSingleResponse("库存不足了哦");
                    }
                }

                // 又添加库存才会走到这里，需要查询实时库存
                ProductDo newProductDo = productMapper.selectById(id);
                if (newProductDo == null) {
                    log.error("商品不存在...");
                    return buildErrorSingleResponse("商品不存在...");
                }
                if (newProductDo.getInventory() < 1) {
                    markNoStock(newProductDo.getGoodsNo());
                }
                if ((newProductDo.getInventory() + appendInventory) < 0) {
                    log.error("库存不足...");
                    return buildErrorSingleResponse("库存不足...");
                }
                log.info("retry..." + id);
                updateInventory(newProductDo, newProductDo.getInventory(), appendInventory);
            }
        }

        updateProductCache(productDo);
        return SingleResponse.ok();
    }
}
