package com.aircraftcarrier.marketing.store.app.test.executor;

import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.marketing.store.domain.gateway.ProductGateway;
import com.aircraftcarrier.marketing.store.infrastructure.gateway.ProductGatewayImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.io.Serializable;

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
    ProductGateway productGateway;


    /**
     * deductionInventory
     *
     * <p>
     * 优化：500毫秒内的用户请求（可能很多上万个请求）合并处理（为了快速完成）， 做mysql批量处理（批量扣库存失败了怎么办？）
     * 参考思路：https://www.bilibili.com/video/BV1g34y1h71Y/?spm_id_from=333.788&vd_source=5ae6c4b2dbcbc1516cef3f31fbe2abb2
     * <p>
     * 另辟蹊径思考：
     * 1. 当同时过来100万个请求抢1万个商品，第一个请求先查库存放内存里，再根据商品创建一个atomic计数器
     * 2. 那么2台机器，每台机器极端情况下可以保证最多只有1万个请求，总共2万个请求
     * 3. 2万个请求怎么只拿到1万个请求呢？ 自增自减？
     * 4. 1万个请求扣减redis库存，insert到mysql流水（保证仅有1万个记录就成功了，后续就可以异步任务处理了）
     * 这样整个过程是不是就保证避免超卖且很快完成了？
     * 思考：能不能每1000条批量batchInsert？ 批量超卖怎么办？（也有可能前9批成功，最后一批超卖了）
     * 失败后，退化为for循环串行执行
     */
    @Transactional(rollbackOn = Exception.class)
    public SingleResponse<Void> deductionInventory(Serializable goodsNo) {
        Integer stock = ProductGatewayImpl.ZERO_STOCK_CACHE.getIfPresent(goodsNo);
        if (stock != null) {
            log.error("库存不足了哦");
            return SingleResponse.error("库存不足了哦");
        }
        //  优化：可以为500毫秒内的用户请求合并处理的结果，deductionNum需要扣除的总库存
        // user -> 订单 -> 商品 -> 扣减数量
        // step01：insert流水记录。。。
        // step02：扣库存
        int deductionNum = 1;
        SingleResponse<Void> response = productGateway.deductionInventory(goodsNo, deductionNum);
        if (!response.success()) {
            // 拆分用户请求， 退化为for循环执行
        }
        return response;
    }

}
