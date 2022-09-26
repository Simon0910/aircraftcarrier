package com.aircraftcarrier.marketing.store.app.test.executor;

import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.framework.tookit.ThreadPoolUtil;
import com.aircraftcarrier.marketing.store.client.product.request.InventoryRequest;
import com.aircraftcarrier.marketing.store.domain.gateway.ProductGateway;
import com.aircraftcarrier.marketing.store.infrastructure.gateway.ProductGatewayImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    /**
     * 2 threads
     */
    private static final ThreadPoolExecutor THREAD_POOL_OFFER = ThreadPoolUtil.newDefaultThreadPool("offer");

    /**
     * 2 threads
     */
    private static final ThreadPoolExecutor THREAD_POOL = ThreadPoolUtil.newFixedThreadPoolDiscardPolicy(2, "merge");

    /**
     * REQUEST_QUEUE
     */
    private static final LinkedBlockingQueue<RequestPromise> REQUEST_QUEUE = new LinkedBlockingQueue<>(50000);

    /**
     * ProductGateway
     */
    @Resource
    ProductGateway productGateway;

    /**
     * init
     */
    @PostConstruct
    private void init() {
        Object lock = new Object();
        // list
        List<RequestPromise> batchList = new ArrayList<>(50000);

        // takeThread 获取用户请求
        THREAD_POOL.execute(() -> {
            int i = 0;
            while (true) {
                try {
                    // take
                    final RequestPromise requestPromise = REQUEST_QUEUE.take();
                    //  add
                    synchronized (lock) {
                        batchList.add(requestPromise);
                    }
                } catch (Exception e) {
                    log.error("takeThread error: ", e);
                }
            }
        });

        // mergeThread 合并用户请求
        THREAD_POOL.execute(() -> {
            while (true) {
                try {
                    if (batchList.size() > 0) {
                        synchronized (lock) {
                            log.info("merge size: {}", batchList.size());

                            // 模拟扣库存
                            TimeUnit.MILLISECONDS.sleep(100);
//                            SingleResponse<Void> response = productGateway.deductionInventory(inventoryRequest.getGoodsNo(), inventoryRequest.getCount());
//                            if (!response.success()) {
//                                // 拆分用户请求， 退化为for循环执行
//                            }

                            for (RequestPromise requestPromise : batchList) {
                                requestPromise.setResult(new Result(true, "ok"));
                                synchronized (requestPromise) {
                                    requestPromise.notify();
                                }
                            }
                            batchList.clear();
                        }
                        log.debug("wait add...");
                        TimeUnit.MILLISECONDS.sleep(200);
                    } else {
                        log.debug("wait merge...");
                        TimeUnit.MILLISECONDS.sleep(1000);
                    }
                } catch (Exception e) {
                    log.error("mergeThread error: ", e);
                }
            }
        });
    }

    /**
     * deductionInventory
     *
     * <p>
     * 优化：200毫秒内的用户请求（可能很多上万个请求）合并处理（为了快速完成）， 做mysql批量处理（批量扣库存失败了怎么办？）
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
    public SingleResponse<Void> deductionInventory(InventoryRequest inventoryRequest) {
        Integer stock = ProductGatewayImpl.ZERO_STOCK_CACHE.getIfPresent(inventoryRequest.getGoodsNo());
        if (stock != null) {
            log.error("库存不足了哦");
            return SingleResponse.error("库存不足了哦");
        }

//        SingleResponse<Void> response = productGateway.deductionInventory(inventoryRequest.getGoodsNo(), inventoryRequest.getCount());

        //  优化：可以为200毫秒内的用户请求合并处理的结果，deductionNum需要扣除的总库存
        // user -> 订单 -> 商品 -> 扣减数量
        // step01：insert流水记录。。。
        // step02：扣库存
        Future<Result> future = ThreadPoolUtil.submit(THREAD_POOL_OFFER, () -> doDeductionInventory(inventoryRequest));

        Result result;
        try {
            result = future.get(500, TimeUnit.MILLISECONDS);
            System.out.println(Thread.currentThread().getName() + ":客户端请求响应:" + result);
            if (result == null) {
                return SingleResponse.error("没等到结果");
            }
        } catch (Exception e) {
            log.error("get Exception: ", e);
            return SingleResponse.error("get Exception");
        }

        if (!result.isSuccess()) {
            // 超时，发送请求回滚
            System.out.println(result.getMsg() + "发起回滚操作");
            return SingleResponse.error("发起回滚操作");
        }

        return SingleResponse.ok();
    }

    /**
     * 用户库存扣减
     *
     * @param inventoryRequest inventoryRequest
     * @return Result
     */
    public Result doDeductionInventory(InventoryRequest inventoryRequest) throws InterruptedException {
        RequestPromise requestPromise = new RequestPromise(inventoryRequest);
        boolean enqueueSuccess = REQUEST_QUEUE.offer(requestPromise, 100, TimeUnit.MILLISECONDS);
        if (!enqueueSuccess) {
            return new Result(false, "系统繁忙");
        }

        // 可以采用页面异步获取结果，比较靠谱一点
//        synchronized (requestPromise) {
//            try {
//                requestPromise.wait(500);
//                if (requestPromise.getResult() == null) {
//                    return new Result(false, "等待超时");
//                }
//            } catch (InterruptedException e) {
//                return new Result(false, "被中断");
//            }
//        }
        return requestPromise.getResult();
    }
}

@Data
class RequestPromise {
    private InventoryRequest inventoryRequest;
    private Result result;

    public RequestPromise(InventoryRequest inventoryRequest) {
        this.inventoryRequest = inventoryRequest;
    }
}

@Data
class Result {
    private Boolean success;
    private String msg;

    public Result(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public boolean isSuccess() {
        return Boolean.TRUE.equals(success);
    }
}
