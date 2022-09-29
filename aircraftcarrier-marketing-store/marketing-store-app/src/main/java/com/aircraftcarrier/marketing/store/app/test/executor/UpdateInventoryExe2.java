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
import java.util.concurrent.ExecutionException;
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
    private static final LinkedBlockingQueue<PromiseRequest> REQUEST_QUEUE = new LinkedBlockingQueue<>(50000);

    /**
     * ProductGateway
     */
    @Resource
    ProductGateway productGateway;

    /**
     * init
     * https://developer.aliyun.com/article/856163
     * https://github.com/trunks2008/RequestMerge
     */
    @PostConstruct
    private void init1() {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(() -> {

            int size = REQUEST_QUEUE.size();
            if (size == 0) {
                log.debug("wait add...");
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                return;
            }

            List<PromiseRequest> requests = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                PromiseRequest request = REQUEST_QUEUE.poll();
                requests.add(request);
            }
            System.out.println("批量处理了" + size + "条请求");

            Integer totalDeductionNum = 0;
            for (PromiseRequest request : requests) {
                totalDeductionNum += request.getInventoryRequest().getCount();
            }

//            // 模拟扣库存
//            try {
//                TimeUnit.MILLISECONDS.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            //返回请求
//            for (PromiseRequest request : requests) {
//                request.future.complete(SingleResponse.ok());
//            }

            SingleResponse<Void> response = productGateway.deductionInventory(requests.get(0).getInventoryRequest().getGoodsNo(), totalDeductionNum);
            if (response.success()) {
                //返回请求
                for (PromiseRequest request : requests) {
                    request.future.completeAsync(() -> response);
                }
            } else {
                // 退化为 二分法 ==》 再到for循环 继续扣减
                for (PromiseRequest request : requests) {
                    request.future.completeAsync(() -> response);
                }
            }

            // 等待 offer
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException ignored) {
            }
        }, 0, 1, TimeUnit.MILLISECONDS);
    }


    /**
     * init
     */
//    @PostConstruct
    private void init2() {
        Object lock = new Object();
        // list
        List<PromiseRequest> batchList = new ArrayList<>(50000);

        // takeThread 获取用户请求
        THREAD_POOL.execute(() -> {
            while (true) {
                try {
                    // take
                    final PromiseRequest request = REQUEST_QUEUE.take();
                    //  add
                    synchronized (lock) {
                        batchList.add(request);
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
//                            // 模拟扣库存
//                            try {
//                                TimeUnit.MILLISECONDS.sleep(100);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            //返回请求
//                            for (PromiseRequest request : batchList) {
//                                request.future.complete(SingleResponse.ok());
//                            }

                            String goodsNo = batchList.get(0).getInventoryRequest().getGoodsNo();
                            int totalDeductionNum = batchList.stream().mapToInt(e -> e.getInventoryRequest().getCount()).sum();
                            SingleResponse<Void> response = productGateway.deductionInventory(goodsNo, totalDeductionNum);
                            if (response.success()) {
                                //返回请求
                                for (PromiseRequest request : batchList) {
                                    request.future.completeAsync(() -> response);
                                }
                            } else {
                                // 退化为 二分法 ==》 再到for循环 继续扣减
                                for (PromiseRequest request : batchList) {
                                    request.future.completeAsync(() -> response);
                                }
                            }

                            batchList.clear();
                        }
                        log.debug("wait add...");
                        TimeUnit.MILLISECONDS.sleep(1);
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
     */
    public SingleResponse<Void> deductionInventory(InventoryRequest inventoryRequest) throws InterruptedException, ExecutionException {
        Integer stock = ProductGatewayImpl.ZERO_STOCK_CACHE.getIfPresent(inventoryRequest.getGoodsNo());
        if (stock != null) {
            log.error("库存不足了哦");
            return SingleResponse.error("库存不足了哦");
        }

        PromiseRequest request = new PromiseRequest();
        request.setInventoryRequest(inventoryRequest);
        CompletableFuture<SingleResponse<Void>> future = new CompletableFuture<>();
        request.setFuture(future);

        boolean enqueueSuccess = REQUEST_QUEUE.offer(request, 100, TimeUnit.MILLISECONDS);
        if (!enqueueSuccess) {
            return SingleResponse.error("系统繁忙");
        }

        return future.get();
        // 如果不同步获取结果，可达到极限速度，可采用另外一个接口获取轮询结果
//        return SingleResponse.error("get 〒_〒");
    }


    /**
     * 封装请求
     */
    @Data
    class PromiseRequest {
        InventoryRequest inventoryRequest;
        CompletableFuture<SingleResponse<Void>> future;
    }
}
