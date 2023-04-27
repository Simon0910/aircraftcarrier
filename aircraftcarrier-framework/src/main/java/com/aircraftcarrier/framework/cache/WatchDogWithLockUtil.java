package com.aircraftcarrier.framework.cache;

import com.aircraftcarrier.framework.concurrent.ExecutorUtil;
import com.aircraftcarrier.framework.tookit.ApplicationContextUtil;
import com.aircraftcarrier.framework.tookit.SleepUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author lzp
 */
@Slf4j
public class WatchDogWithLockUtil {
    private static final ExecutorService executorService = ExecutorUtil.newCachedThreadPoolDiscard(1, "watch-dog");
    private Map<String, Thread> lockRecord;
    private RedisLockRenewal redisLockRenewal;

    private WatchDogWithLockUtil() {
    }

    public static WatchDogWithLockUtil getInstance() {
        return WatchDogWithLockUtil.Singleton.getInstance();
    }

    private static RedisLockRenewal getRedisLockRenewal() {
        return WatchDogWithLockUtil.ResourceHolder.REDIS_LOCK_RENEWAL;
    }

    void init(Map<String, Thread> lockRecord) {
        if (ApplicationContextUtil.notContains("redisLockRenewal")) {
            log.error("need a bean, but not found bean name [redisLockRenewal]");
            return;
        }

        this.lockRecord = lockRecord;
        this.redisLockRenewal = ApplicationContextUtil.getBean(RedisLockRenewal.class);
    }

    void startUp() {
        if (redisLockRenewal == null) {
            redisLockRenewal = getRedisLockRenewal();
        }
        executorService.execute(this::renewal);
    }

    /**
     * init
     */
    private void renewal() {
        log.info("periodic renewal start...");
        while (true) {
            // 10s
            SleepUtil.sleepSeconds(10);

            if (lockRecord.isEmpty()) {
                log.info("lockRecord is empty!");
                break;
            }

            Map<Serializable, Thread> lockRecordCopy = new HashMap<>(lockRecord);

            lockRecordCopy.forEach((key, thread) -> {
                if (thread.isAlive()) {
                    redisLockRenewal.renewalKey(key, 30);
                }
            });
        }
        log.info("periodic renewal end.");
    }

    /**
     * 静态内部类的方式, 初始化单例
     */
    private static class Singleton {
        /**
         * 实例
         */
        private static final WatchDogWithLockUtil INSTANCE;

        static {
            INSTANCE = new WatchDogWithLockUtil();
        }

        /**
         * getInstance
         *
         * @return WatchDog
         */
        public static WatchDogWithLockUtil getInstance() {
            return INSTANCE;
        }
    }

    private static class ResourceHolder {
        private static final RedisLockRenewal REDIS_LOCK_RENEWAL = ApplicationContextUtil.getBean(RedisLockRenewal.class); // This will be lazily initialised
    }
}
