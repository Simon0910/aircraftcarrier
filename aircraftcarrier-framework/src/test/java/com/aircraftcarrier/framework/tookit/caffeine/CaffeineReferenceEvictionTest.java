package com.aircraftcarrier.framework.tookit.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.Test;

/**
 * @author xifanxiaxue
 * @date 2020/11/25 0:43
 * @desc 基于引用淘汰
 * <p>
 * 这里要注意的地方有三个
 * <p>
 * System.gc() 不一定会真的触发GC，只是一种通知机制，但是并非一定会发生GC，垃圾收集器进不进行GC是不确定的，所以有概率看到设置weakKeys了却在调用System.gc() 的时候却没有丢失缓存数据的情况。
 * 使用异步加载的方式不允许使用引用淘汰机制，启动程序的时候会报错：java.lang.IllegalStateException: Weak or soft values can not be combined with AsyncCache，猜测原因是异步加载数据的生命周期和引用淘汰机制的生命周期冲突导致的，因而Caffeine不支持。
 * 使用引用淘汰机制的时候，判断两个key或者两个value是否相同，用的是 ==，而非是equals()，也就是说需要两个key指向同一个对象才能被认为是一致的，这样极可能导致缓存命中出现预料之外的问题。
 * 因而，总结下来就是慎用基于引用的淘汰机制，其实其他的淘汰机制完全够用了。
 */
public class CaffeineReferenceEvictionTest {

    @Test
    public void testWeak() {
        Cache<Integer, Integer> cache = Caffeine.newBuilder()
                // 设置Key为弱引用，生命周期是下次gc的时候
                .weakKeys()
                // 设置value为弱引用，生命周期是下次gc的时候
                .weakValues()
                .build();
        cache.put(1, 2);
        System.out.println(cache.getIfPresent(1));

        // 强行调用一次GC
        System.gc();

        System.out.println(cache.getIfPresent(1));
    }

    @Test
    public void testSoft() {
        Cache<Integer, Integer> cache = Caffeine.newBuilder()
                // 设置value为软引用，生命周期是GC时并且堆内存不够时触发清除
                .softValues()
                .build();
        cache.put(1, 2);
        System.out.println(cache.getIfPresent(1));

        // 强行调用一次GC
        System.gc();

        System.out.println(cache.getIfPresent(1));
    }
}