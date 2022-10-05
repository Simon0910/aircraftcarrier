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
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
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
public class UpdateInventoryExe2 {
    /**
     * threads
     */
    private static final int N_THREADS = 2;

    /**
     * Pool
     */
    private static final ThreadPoolExecutor THREAD_POOL = ThreadPoolUtil.newFixedThreadPoolDiscardPolicy(N_THREADS, "merge");

    /**
     * 批量处理 可配置
     */
    private final int batchSize = 500;

    /**
     * waitTimeout 可配置
     */
    private final long waitTimeout = 20;

    /**
     * REQUEST_QUEUE
     */
    private static final LinkedBlockingQueue<PromiseRequest> REQUEST_QUEUE = new LinkedBlockingQueue<>(50000);

    /**
     * ProductGateway
     */
    @Resource
    ProductGateway productGateway;

    @PostConstruct
    private void init() {
//        init1();
        for (int i = 0; i < N_THREADS; i++) {
            init2();
        }
    }

    /**
     * init
     * https://developer.aliyun.com/article/856163
     * https://github.com/trunks2008/RequestMerge
     */
    private void init1() {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            List<PromiseRequest> batchList = new ArrayList<>(batchSize);
            // empty wait put...
            int size = REQUEST_QUEUE.size();
            if (size < 1) {
                log.debug("wait put...");
                PromiseRequest firstRequest;
                try {
                    firstRequest = REQUEST_QUEUE.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                batchList.add(firstRequest);
                size = REQUEST_QUEUE.size();
            }

            // 批量太少，等待200毫秒超时(参考Kafka)
            if (size + batchList.size() < batchSize) {
                try {
                    TimeUnit.MILLISECONDS.sleep(waitTimeout);
                } catch (InterruptedException ignored) {
                }
                size = REQUEST_QUEUE.size();
            }
            size = Math.min(size, batchList.size() > 0 ? batchSize - 1 : batchSize);

            // 处理逻辑
            try {
                for (int i = 0; i < size; i++) {
                    PromiseRequest request = REQUEST_QUEUE.poll();
                    if (request == null) {
                        break;
                    }
                    batchList.add(request);
                }
                System.out.println("批量处理了" + batchList.size() + "条请求");

                Integer totalDeductionNum = 0;
                for (PromiseRequest request : batchList) {
                    totalDeductionNum += request.getInventoryRequest().getCount();
                }

//                // 模拟扣库存
//                try {
//                    TimeUnit.MILLISECONDS.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                //返回请求
//                for (PromiseRequest request : batchList) {
//                    request.future.completeAsync(SingleResponse.ok());
//                }

                SingleResponse<Void> batchResponse = productGateway.deductionInventory(batchList.get(0).getInventoryRequest().getGoodsNo(), totalDeductionNum);
                if (batchResponse.success()) {
                    //返回请求
                    for (PromiseRequest request : batchList) {
                        request.future.completeAsync(() -> batchResponse);
                    }
                } else {
                    // 退化为 二分法 ==》 再到for循环 继续扣减
                    log.info("for size: {}", batchList.size());
                    for (PromiseRequest request : batchList) {
                        SingleResponse<Void> perResponse = productGateway.deductionInventory(batchList.get(0).getInventoryRequest().getGoodsNo(), request.getInventoryRequest().getCount());
                        request.future.completeAsync(() -> perResponse);
                    }
                }
            } catch (Exception e) {
                log.error("mergeThread error: ", e);

                //返回请求
                for (PromiseRequest request : batchList) {
                    request.future.completeAsync(() -> SingleResponse.error("处理异常"));
                }

                try {
                    // 死循环避免cpu飙升，发送告警
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        }, 0, 1, TimeUnit.MILLISECONDS);
    }


    /**
     * init
     */
    private void init2() {
        // mergeThread 合并用户请求
        THREAD_POOL.execute(() -> {
            while (true) {
                List<PromiseRequest> batchList = new ArrayList<>(batchSize);
                // empty wait put...
                int size = REQUEST_QUEUE.size();
                if (size < 1) {
                    log.debug("wait put...");
                    PromiseRequest firstRequest;
                    try {
                        firstRequest = REQUEST_QUEUE.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        continue;
                    }
                    batchList.add(firstRequest);
                    size = REQUEST_QUEUE.size();
                }

                // 批量太少，等待200毫秒超时(参考Kafka)
                if (size + batchList.size() < batchSize) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(waitTimeout);
                    } catch (InterruptedException ignored) {
                    }
                    size = REQUEST_QUEUE.size();
                }
                size = Math.min(size, batchList.size() > 0 ? batchSize - 1 : batchSize);

                // 处理逻辑
                try {
                    for (int i = 0; i < size; i++) {
                        PromiseRequest request = REQUEST_QUEUE.poll();
                        if (request == null) {
                            break;
                        }
                        batchList.add(request);
                    }
                    log.info("merge size: {}", batchList.size());
                    if (batchList.isEmpty()) {
                        continue;
                    }

//                    // 模拟扣库存
//                    try {
//                        TimeUnit.MILLISECONDS.sleep(100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    //返回请求
//                    for (PromiseRequest request : batchList) {
//                        request.future.completeAsync(SingleResponse.ok());
//                    }

                    String goodsNo = batchList.get(0).getInventoryRequest().getGoodsNo();
                    int totalDeductionNum = batchList.stream().mapToInt(e -> e.getInventoryRequest().getCount()).sum();
                    SingleResponse<Void> batchResponse = productGateway.deductionInventory(goodsNo, totalDeductionNum);
                    if (batchResponse.success()) {
                        //返回请求
                        for (PromiseRequest request : batchList) {
                            request.future.completeAsync(() -> batchResponse);
                        }
                    } else {
                        // 退化为 二分法 ==》 再到for循环 继续扣减
                        log.info("for size: {}", batchList.size());
                        for (PromiseRequest request : batchList) {
                            SingleResponse<Void> perResponse = productGateway.deductionInventory(goodsNo, request.getInventoryRequest().getCount());
                            request.future.completeAsync(() -> perResponse);
                        }
                    }

                } catch (Exception e) {
                    log.error("mergeThread error: ", e);

                    //返回请求
                    for (PromiseRequest request : batchList) {
                        request.future.completeAsync(() -> SingleResponse.error("处理异常"));
                    }

                    try {
                        // 死循环避免cpu飙升，发送告警
                        TimeUnit.MILLISECONDS.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }


                }
            }

        });
    }


    /**
     * deductionInventory
     */
    public SingleResponse<Void> deductionInventory(InventoryRequest inventoryRequest) {
        Integer stock = ProductGatewayImpl.ZERO_STOCK_CACHE.getIfPresent(inventoryRequest.getGoodsNo());
        if (stock != null) {
            log.error("库存不足了哦");
            return SingleResponse.error("库存不足了哦");
        }

//        return productGateway.deductionInventory(inventoryRequest.getGoodsNo(), inventoryRequest.getCount());

        PromiseRequest request = new PromiseRequest();
        request.setInventoryRequest(inventoryRequest);
        CompletableFuture<SingleResponse<Void>> future = new CompletableFuture<>();
        request.setFuture(future);

        try {
            boolean enqueueSuccess = REQUEST_QUEUE.offer(request, 100, TimeUnit.MILLISECONDS);
            if (!enqueueSuccess) {
                log.error("系统繁忙");
                return SingleResponse.error("系统繁忙");
            }
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

//        // 如果不同步获取结果，可达到极限速度，可采用另外一个接口获取轮询结果
//        return SingleResponse.error("get 〒_〒");
    }


    /**
     * 封装请求
     */
    @Data
    static class PromiseRequest {
        InventoryRequest inventoryRequest;
        CompletableFuture<SingleResponse<Void>> future;
    }
}
