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
        if (deductionNum >= 0) {
            throw new SysException("deductionNum must be less than 0");
        }
        return updateInventory(goodsNo, deductionNum);
    }

    public SingleResponse<Void> updateInventory(Serializable goodsNo, Integer appendInventory) {
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

//        return updateInventory(productDo.getId(), productDo.getVersion(), productDo.getInventory(), appendInventory);
        return updateInventory(productDo.getId(), productDo.getInventory(), appendInventory);
    }

    /**
     * 更新库存1
     * 与用户侧 与 运营侧无关，底层基础设施通用方法
     *
     * @param id              数据主键 （非业务主键）
     * @param version         数据更新版本号
     * @param originInventory 原始库存
     * @param appendInventory 追加扣减库存（正负代表加减库存）
     * @return SingleResponse
     */
    private SingleResponse<Void> updateInventory(Long id, Long version, Integer originInventory, Integer appendInventory) {
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

    /**
     * 更新库存2
     * 本地测试：
     * 1000 人抢 990个库存性能比 更新库存1 提升5倍（614ms，150ms） why？
     * 排查：观察日志发现 方法1日志量在33323行，返回到应用内重试次数为370， 方法2日志量30256行, 返回到应用内重试94次
     * 原因：方法1利用version乐观锁导致大量请求返回到应用内串行执行，返回到应用内重试次数为370，方法2仅仅在mysql层串行执行，返回到应用内重试94次
     * 当增加到 10000 人抢 9990 个库存 方法2也不行了，
     * 排查：程序超时间执行不完了
     * 原因：因为大量的请求导致 mysql的io 飙升！
     * 500毫秒内的用户请求（可能很多上万个请求）合并处理（为了快速完成）， 做批量请求（批量扣库存失败了怎么办？）
     * 参考思路：https://www.bilibili.com/video/BV1g34y1h71Y/?spm_id_from=333.788&vd_source=5ae6c4b2dbcbc1516cef3f31fbe2abb2
     *
     * @param id              数据主键 （非业务主键）
     * @param originInventory 原始库存
     * @param appendInventory 追加扣减库存（正负代表加减库存）
     * @return SingleResponse
     */
    private SingleResponse<Void> updateInventory(Long id, Integer originInventory, Integer appendInventory) {
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

        int updatedNum = productMapper.updateInventoryDirect(id, appendInventory);
        if (updatedNum < 1) {
            synchronized (id.toString().intern()) {
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
                updateInventory(id, productDo.getInventory(), appendInventory);
            }
        }
        return SingleResponse.ok();
    }

}
