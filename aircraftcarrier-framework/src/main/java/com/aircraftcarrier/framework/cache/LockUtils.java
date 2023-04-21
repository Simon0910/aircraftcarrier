package com.aircraftcarrier.framework.cache;

import com.aircraftcarrier.framework.cache.suport.MyLockTemplate;
import com.aircraftcarrier.framework.exception.LockNotAcquiredException;
import com.aircraftcarrier.framework.tookit.ApplicationContextUtil;
import com.aircraftcarrier.framework.tookit.MapUtil;
import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author lzp
 */
@Slf4j
public class LockUtils {

    /**
     * key: request
     */
    private static final Map<Thread, Map<String, Request>> THREAD_LOCAL = MapUtil.newConcurrentHashMap(1000);

    /**
     * key: queue
     */
    private static final Map<String, SingularUpdateQueue<Request, Request>> KEY_QUEUE = MapUtil.newConcurrentHashMap(1000);

    private static final Map<String, Thread> LOCK_RECORD = new ConcurrentHashMap<>();

    static {
        WatchDogWithLockUtils.getInstance().init(LOCK_RECORD);
    }

    private LockUtils() {
    }

    private static MyLockTemplate getMyLockTemplate() {
        return LockUtils.ResourceHolder.MY_LOCK_TEMPLATE;
    }

    @NotNull
    private static SingularUpdateQueue<Request, Request> getQueue(String key) {
        return KEY_QUEUE.compute(key, (k, v) -> v == null ? new SingularUpdateQueue<>(LockUtils::doRedisLock) : v);
    }

    private static SingularUpdateQueue<Request, Request> getNewQueue(String key) {
        SingularUpdateQueue<Request, Request> oldV = KEY_QUEUE.get(key);
        synchronized (LockUtils.class) {
            SingularUpdateQueue<Request, Request> cur = KEY_QUEUE.get(key);
            if (cur != oldV) {
                return cur;
            }
            SingularUpdateQueue<Request, Request> newV = new SingularUpdateQueue<>(LockUtils::doRedisLock);
            KEY_QUEUE.put(key, newV);
            return newV;
        }
    }

    public static void lock(String key, long expire, long timeout) throws LockNotAcquiredException {
        if (!doLock(key, expire, timeout)) {
            throw new LockNotAcquiredException("do not locked");
        }
    }

    public static boolean tryLock(String key, long expire, long timeout) {
        return doLock(key, expire, timeout);
    }

    private static boolean doLock(String key, long expire, long timeout) {
        Map<String, Request> keyMap = THREAD_LOCAL.get(Thread.currentThread());
        Request preRequest;
        if (keyMap != null && (preRequest = keyMap.get(key)) != null) {
            // 可重入
            preRequest.incr();
            return true;
        }

        Request request = new Request(key, expire, timeout);
        SingularUpdateQueue<Request, Request> keyQueue = getQueue(key);
        if (!keyQueue.isAlive()) {
            keyQueue = getNewQueue(key);
        }

        try {
            CompletableFuture<Request> f = keyQueue.submit(request, timeout, TimeUnit.MILLISECONDS);

            Request getRequest = f.get();
            if (getRequest == null) {
                log.error("submit timeout");
                return false;
            }
            if (getRequest.isNotLocked()) {
                log.error("not locked " + getRequest.getErrorMessage());
                return false;
            }
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("doLock interrupted ", e);
            return false;
        } catch (Exception e) {
            log.error("doLock exception ", e);
            return false;
        }
    }

    private static Request doRedisLock(Request request) {
        if (System.currentTimeMillis() - request.getRequestTime() > request.getTimeout()) {
            // 请求超时
            request.setErrorMessage("waiting timeout");
            return request;
        }

        Map<String, Request> keyMap = THREAD_LOCAL.get(request.getRequestThread());
        Request preRequest;
        if (keyMap != null && (preRequest = keyMap.get(request.getLockKey())) != null) {
            // 可重入
            preRequest.incr();
            // 返回带有已经加锁的 当前线程之前的request
            return preRequest;
        }

        try {
            LockInfo lockInfo = getMyLockTemplate().lock(request.getLockKey(), request.getExpire(), request.getTimeout());
            if (lockInfo == null) {
                request.setErrorMessage("not acquired");
                return request;
            }
            request.setLockInfo(lockInfo);

            keyMap = THREAD_LOCAL.computeIfAbsent(request.getRequestThread(), k -> new HashMap<>(16));
            Request reentryRequest = keyMap.get(request.getLockKey());
            if (reentryRequest == null) {
                keyMap.put(request.getLockKey(), request);
            } else {
                reentryRequest.incr();
            }

            // 登记记录
            LOCK_RECORD.put(request.getLockKey(), request.getRequestThread());
            WatchDogWithLockUtils.getInstance().startUp();
        } catch (Exception e) {
            log.error("doLock error ", e);
            request.setErrorMessage(e.getMessage());
        }
        return request;
    }

    public static void unLock(String key) {
        Map<String, Request> keyMap = THREAD_LOCAL.get(Thread.currentThread());
        Request preRequest;
        if (keyMap != null && (preRequest = keyMap.get(key)) != null) {
            if (preRequest.getAcquireCount() == 0) {
                // 移除登记记录，停止续期
                Thread thread = LOCK_RECORD.get(preRequest.getLockKey());
                if (Thread.currentThread() == thread) {
                    LOCK_RECORD.remove(preRequest.getLockKey());
                }

                keyMap.remove(preRequest.getLockKey());
                if (keyMap.isEmpty()) {
                    THREAD_LOCAL.remove(Thread.currentThread());
                }

                boolean unLocked = doUnLock(preRequest.getLockInfo(), 3);

                if (!unLocked) {
                    log.error("释放锁异常");
                }
            } else {
                preRequest.decr();
            }
        }
    }

    private static boolean doUnLock(LockInfo lockInfo, int retry) {
        try {
            // 释放锁
            getMyLockTemplate().releaseLock(lockInfo);
            return true;
        } catch (Exception e) {
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }

            if (retry > 0) {
                retry--;
                return doUnLock(lockInfo, retry);
            } else {
                return false;
            }
        }
    }

    @Getter
    @Setter
    private static class Request {
        private final Thread requestThread = Thread.currentThread();
        private final long requestTime = System.currentTimeMillis();
        private final String lockKey;
        private final long expire;
        private final long timeout;
        private LockInfo lockInfo;
        // 当前请求重入了几次
        private int acquireCount;

        private String errorMessage;

        private Request(String lockKey, long expire, long timeout) {
            this.lockKey = lockKey;
            this.expire = expire;
            this.timeout = timeout;
        }

        private boolean isLocked() {
            return lockInfo != null;
        }

        private boolean isNotLocked() {
            return !isLocked();
        }

        private void incr() {
            acquireCount++;
        }

        private void decr() {
            acquireCount--;
        }
    }

    private static class ResourceHolder {
        private static final MyLockTemplate MY_LOCK_TEMPLATE = (MyLockTemplate) ApplicationContextUtil.getBean(LockTemplate.class); // This will be lazily initialised
    }
}