package com.aircraftcarrier.marketing.store.app.test.executor;

import com.aircraftcarrier.framework.exception.SysException;
import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.framework.tookit.LockKeyUtil;
import com.aircraftcarrier.marketing.store.infrastructure.repository.dataobject.ProductDo;
import com.aircraftcarrier.marketing.store.infrastructure.repository.mapper.ProductMapper;
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
 * <p>
 * 前端:
 * 页面静态化, 加入CDN
 * 倒计时
 * 考虑在活动开始前,点击一次后按钮置灰
 * 活动开始后暴露秒杀地址接口+md5
 * url合法性校验
 * 手机号 + 验证码
 * <p>
 * 服务端:
 * 设备及IP信息，筛选出风控用户
 * 防刷接口,对用户访问频率,次数设限
 * 利用黑白名单机制，支持运营配置
 * 内存库存标记
 * redis预减库存
 * 请求入mq队列
 * 万能出错页
 * 限流,熔断,降级策略等
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
        if (list.size() != 1) {
            // 暴漏数据异常，此问题需要从源头解决！！！
            throw new SysException("Duplicate keys result in objects that cannot be mapped");
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
        if (appendInventory == 0) {
            // 避免无效递增版本号，无需不发送MQ库存变更通知，若更新所有字段和数据库相同避免死循环
            log.warn("库存无变化");
            return SingleResponse.error("库存无变化");
        }

        int newInventory = originInventory + appendInventory;
        if (newInventory < 0) {
            log.warn("库存不足");
            return SingleResponse.error("库存不足");
        }

        int updatedNum = productMapper.updateInventory(id, version, newInventory);
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
