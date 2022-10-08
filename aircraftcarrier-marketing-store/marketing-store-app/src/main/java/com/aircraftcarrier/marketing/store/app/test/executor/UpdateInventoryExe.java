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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

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
     * capacity
     */
    private static final int CAPACITY = 50000;

    /**
     * REQUEST_QUEUE
     */
    private static final LinkedBlockingQueue<RequestPromise> REQUEST_QUEUE = new LinkedBlockingQueue<>(CAPACITY);

    /**
     * 2 threads
     */
    private static final ThreadPoolExecutor THREAD_POOL = ThreadPoolUtil.newFixedThreadPoolDiscardPolicy(2, "merge");
    /**
     * 批量处理 可配置
     */
    private final int batchSize = 500;
    /**
     * waitTimeout 可配置
     */
    private final long waitTimeout = 20;
    /**
     * needSignal
     */
    private volatile boolean needSignal = true;

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
        final ReentrantLock takeLock = new ReentrantLock();
        final Condition notEmpty = takeLock.newCondition();

        Object lock = new Object();
        // list
        List<RequestPromise> batchList = new ArrayList<>(CAPACITY);

        // takeThread 获取用户请求
        THREAD_POOL.execute(() -> {
            while (true) {
                try {
                    // take
                    RequestPromise requestPromise = REQUEST_QUEUE.take();
                    //  add
                    synchronized (lock) {
                        batchList.add(requestPromise);

                        if (needSignal) {
                            takeLock.lock();
                            try {
                                notEmpty.signal();
                            } finally {
                                takeLock.unlock();
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("takeThread error: ", e);
                }
            }
        });

        // mergeThread 合并用户请求
        THREAD_POOL.execute(() -> {
            while (true) {
                // wait put...
                if (batchList.size() < 1) {
                    log.info("wait put...");
                    takeLock.lock();
                    try {
                        while (batchList.size() < 1) {
                            needSignal = true;
                            notEmpty.await();
                        }
                        if (needSignal) {
                            needSignal = false;
                        }
                    } catch (InterruptedException ignored) {
                    } finally {
                        takeLock.unlock();
                    }
                    log.info("wake up...");
                }

                // 批量太少，等待200毫秒超时(参考Kafka)
                if (batchList.size() < batchSize) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(waitTimeout);
                    } catch (InterruptedException ignored) {
                    }
                }

                // 处理逻辑
                synchronized (lock) {
                    try {
                        log.info("merge size: {}", batchList.size());

//                        // 模拟扣库存
//                        try {
//                            TimeUnit.MILLISECONDS.sleep(10);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        //返回请求
//                        for (RequestPromise request : batchList) {
//                            request.getFuture().completeAsync(SingleResponse.ok());
//                        }

                        String goodsNo = batchList.get(0).getInventoryRequest().getGoodsNo();
                        int totalDeductionNum = batchList.stream().mapToInt(e -> e.getInventoryRequest().getCount()).sum();
                        SingleResponse<Void> batchResponse = productGateway.deductionInventory(goodsNo, totalDeductionNum);
                        if (batchResponse.success()) {
                            //返回请求
                            for (RequestPromise request : batchList) {
                                request.getFuture().completeAsync(() -> batchResponse);
                            }
                        } else {
                            // 退化为 二分法 ==》 再到for循环 继续扣减
                            log.info("for size: {}", batchList.size());
                            for (RequestPromise request : batchList) {
                                SingleResponse<Void> perResponse = productGateway.deductionInventory(batchList.get(0).getInventoryRequest().getGoodsNo(), request.getInventoryRequest().getCount());
                                request.getFuture().completeAsync(() -> perResponse);
                            }
                        }

                    } catch (Throwable e) {
                        log.error("mergeThread error: ", e);

                        //返回请求
                        for (RequestPromise request : batchList) {
                            request.getFuture().completeAsync(() -> SingleResponse.error("处理异常"));
                        }

                        try {
                            // 死循环避免cpu飙升，发送告警
                            TimeUnit.MILLISECONDS.sleep(1000);
                        } catch (InterruptedException ignored) {
                        }
                    } finally {
                        batchList.clear();
                    }
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
     * https://github.com/JiHaiChannel/demo
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
        RequestPromise request = new RequestPromise(inventoryRequest);
        CompletableFuture<SingleResponse<Void>> future = new CompletableFuture<>();
        request.setFuture(future);

        try {
            REQUEST_QUEUE.put(request);
        } catch (InterruptedException e) {
            log.error("系统繁忙", e);
            return SingleResponse.error("系统繁忙");
        }

        try {
            return future.get();
        } catch (Exception e) {
            // 库存是否回滚
            log.error("系统异常", e);
            return SingleResponse.error("系统繁忙");
        }
//        // 如果不获取结果，可达到极限速度，可采用另外一个接口获取轮询结果
//        return SingleResponse.error("get 〒_〒");
    }

    @Data
    static class RequestPromise {
        private InventoryRequest inventoryRequest;
        private CompletableFuture<SingleResponse<Void>> future;

        public RequestPromise(InventoryRequest inventoryRequest) {
            this.inventoryRequest = inventoryRequest;
        }
    }
}
