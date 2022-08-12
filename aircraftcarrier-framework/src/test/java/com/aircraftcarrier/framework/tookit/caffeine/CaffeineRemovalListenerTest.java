package com.aircraftcarrier.framework.tookit.caffeine;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Scheduler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author xifanxiaxue
 * @date 2020/11/19 22:34
 * @desc 淘汰通知
 */
public class CaffeineRemovalListenerTest {

    /**
     * 目前数据被淘汰的原因不外有以下几个：
     * <p>
     * EXPLICIT：如果原因是这个，那么意味着数据被我们手动的remove掉了。
     * REPLACED：就是替换了，也就是put数据的时候旧的数据被覆盖导致的移除。
     * COLLECTED：这个有歧义点，其实就是收集，也就是垃圾回收导致的，一般是用弱引用或者软引用会导致这个情况。
     * EXPIRED：数据过期，无需解释的原因。
     * SIZE：个数超过限制导致的移除。
     *
     * @throws InterruptedException
     */
    @Test
    public void test() throws InterruptedException {
        LoadingCache<Integer, Integer> cache = Caffeine.newBuilder()
                .expireAfterAccess(1, TimeUnit.SECONDS)
                .scheduler(Scheduler.systemScheduler())
                // 增加了淘汰监听
                .removalListener(((key, value, cause) -> {
                    System.out.println("淘汰通知，key：" + key + "，原因：" + cause);
                }))
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(@NonNull Integer key) throws Exception {
                        return key;
                    }
                });

        cache.put(1, 2);

        Thread.sleep(2000);
    }

}