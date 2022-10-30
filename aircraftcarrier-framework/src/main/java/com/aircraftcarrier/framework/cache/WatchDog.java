package com.aircraftcarrier.framework.cache;

import com.aircraftcarrier.framework.tookit.ApplicationContextUtil;
import com.aircraftcarrier.framework.tookit.SleepUtil;
import com.aircraftcarrier.framework.tookit.ThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author lzp
 */
@Slf4j
public class WatchDog {
    private static final ExecutorService executorService = ThreadPoolUtil.newCachedThreadPoolDiscard(1, "watch-dog");
    private Map<Serializable, Long> lockRecord;
    private RedisLockRenewal redisLockRenewal;

    private WatchDog() {
    }

    public static WatchDog getInstance() {
        return WatchDog.Singleton.getInstance();
    }

    void init(Map<Serializable, Long> lockRecord) {
        this.lockRecord = lockRecord;
        if (!ApplicationContextUtil.contains("redisLockRenewal")) {
            log.error("need a bean, but not found bean name [redisLockRenewal]");
            return;
        }
        this.redisLockRenewal = ApplicationContextUtil.getBean(RedisLockRenewal.class);
    }

    void startUp() {
        if (redisLockRenewal == null) {
            log.error("need a bean, but not found bean name [redisLockRenewal]");
            return;
        }
        executorService.execute(this::renewal);
    }

    /**
     * init
     */
    private void renewal() {
        while (true) {
            // 10s
            SleepUtil.sleepSeconds(10);

            if (lockRecord.isEmpty()) {
                log.info("lockRecord is empty!");
                return;
            }

            lockRecord.forEach((key, threadId) -> redisLockRenewal.renewalKey(key, 30));
        }

    }

    /**
     * 静态内部类的方式, 初始化单例
     */
    private static class Singleton {
        /**
         * 实例
         */
        private static final WatchDog INSTANCE;

        static {
            INSTANCE = new WatchDog();
        }

        /**
         * getInstance
         *
         * @return WatchDog
         */
        public static WatchDog getInstance() {
            return INSTANCE;
        }
    }

}
