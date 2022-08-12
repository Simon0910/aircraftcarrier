package com.aircraftcarrier.framework.tookit.caffeine;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

/**
 * @author xifanxiaxue
 * @date 2020/12/1 23:12
 * @desc
 */
public class CaffeineRefreshTest {

    private int index = 1;

    /**
     * 模拟从数据库中读取数据
     *
     * @return
     */
    private int getInDB() {
        // 这里为了体现数据重新被get，因而用了index++
        index++;
        return index;
    }

    /**
     * 坑点：
     * 我研究过源码，写后刷新其实并不是方法名描述的那样在一定时间后自动刷新，而是在一定时间后进行了访问，再访问后才自动刷新。
     * 也就是在第一次cache.get(1)的时候其实取到的依旧是旧值，在doAfterRead里边做了自动刷新的操作，
     * 这样在第二次cache.get(1)取到的才是刷洗后的值。
     * 坑点：
     * 在写后刷新被触发后，会重新填充数据，因而会触发写后过期时间机制的重新计算。
     *
     * @throws InterruptedException
     */
    @Test
    public void test() throws InterruptedException {
        // 设置写入后3秒后数据过期，2秒后如果有数据访问则刷新数据
        LoadingCache<Integer, Integer> cache = Caffeine.newBuilder()
                .refreshAfterWrite(2, TimeUnit.SECONDS)
                .expireAfterWrite(3, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer key) {
                        System.out.println("load");
                        return getInDB();
                    }
                });
        cache.put(1, getInDB());

        // 休眠2.5秒，后取值
        Thread.sleep(2500);
        System.out.println(cache.getIfPresent(1));

        // 休眠1.5秒，后取值
        Thread.sleep(1500);
        System.out.println(cache.getIfPresent(1));

    }
}
