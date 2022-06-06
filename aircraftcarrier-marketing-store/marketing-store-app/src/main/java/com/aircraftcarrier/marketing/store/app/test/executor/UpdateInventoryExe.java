package com.aircraftcarrier.marketing.store.app.test.executor;

import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.framework.tookit.LockKeyUtil;
import com.aircraftcarrier.marketing.store.infrastructure.repository.ProductMapper;
import com.aircraftcarrier.marketing.store.infrastructure.repository.dataobject.ProductDo;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 并发扣库存
 * 防止超卖 && 防止数据错乱
 * <p>
 * 本示例只是再mysql层的防护。
 * 秒杀内容还包含：
 * 前端暴露秒杀地址接口， 倒计时， url合法性校验，验证码等
 * 服务端（内存标记，redis预减库存，请求入mq队列）
 *
 * @author lzp
 */
@Slf4j
@Component
public class UpdateInventoryExe {

    @Resource
    private ProductMapper productMapper;

    public SingleResponse<Void> deductionInventory(Serializable goodsNo) {
        return deductionInventory(goodsNo, -1);
    }

    public SingleResponse<Void> deductionInventory(Serializable goodsNo, Integer deductionNum) {
        List<ProductDo> list = new LambdaQueryChainWrapper<>(productMapper)
                .eq(ProductDo::getGoodsNo, goodsNo)
                .list();
        if (list.isEmpty()) {
            log.error("商品不存在");
            return SingleResponse.error("商品不存在");
        }

        ProductDo productDo = list.get(0);

        return updateInventory(productDo.getId(), productDo.getVersion(), productDo.getInventory(), deductionNum);
    }

    /**
     * 更新库存
     * 与用户侧 与 运营侧无关，底层基础设施通用方法
     *
     * @param id              数据主键 （非业务主键）
     * @param version         数据更新版本号
     * @param originInventory 原始库存
     * @param appendInventory 追加扣减库存（正负代表加减库存）
     * @return SingleResponse
     */
    public SingleResponse<Void> updateInventory(Long id, Long version, Integer originInventory, Integer appendInventory) {
        int newInventory = originInventory + appendInventory;
        if (newInventory < 0) {
            log.warn("库存不足");
            return SingleResponse.error("库存不足");
        }

        int updatedNum = productMapper.updateInventory(id, version, newInventory);
        System.out.println("updatedNum = " + updatedNum);
        if (updatedNum < 1) {
            LockKeyUtil.lock(id.toString());
            try {
                ProductDo productDo = productMapper.selectById(id);
                if (productDo == null) {
                    log.error("商品不存在...");
                    return SingleResponse.error("商品不存在...");
                }
                if ((productDo.getInventory() + appendInventory) < 0) {
                    log.warn("库存不足...");
                    return SingleResponse.error("库存不足...");
                }
                log.debug("retry..." + id);
                updateInventory(id, productDo.getVersion(), productDo.getInventory(), appendInventory);
            } finally {
                LockKeyUtil.unlock(id.toString());
            }
        }

        return SingleResponse.ok();
    }

}
