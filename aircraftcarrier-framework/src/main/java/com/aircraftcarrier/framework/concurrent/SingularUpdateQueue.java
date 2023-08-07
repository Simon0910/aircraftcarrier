package com.aircraftcarrier.framework.concurrent;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * <a href="https://github.com/dreamhead/patterns-of-distributed-systems/blob/master/content/singular-update-queue.md">...</a>
 *
 * @author zhipengliu
 */
@Slf4j
public class SingularUpdateQueue<R, S> {
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ArrayBlockingQueue<RequestWrapper<R, S>> workQueue = new ArrayBlockingQueue<>(1024);
    private final Function<R, S> handler;
    private final ExecutorService executorService = ExecutorUtil.newCachedThreadPoolBlock(1, "singular");
    /**
     * <a href="https://www.baeldung.com/java-thread-stop">...</a>
     */
    public SingularUpdateQueue(Function<R, S> handler) {
        this.handler = handler;
    }

    public CompletableFuture<S> submit(R request) throws InterruptedException {
        var requestWrapper = new RequestWrapper<R, S>(request);
        workQueue.put(requestWrapper);
        checkAndStart();
        return requestWrapper.getFuture();
    }

    public CompletableFuture<S> submit(R request, long timeout, TimeUnit unit) throws InterruptedException {
        RequestWrapper<R, S> requestWrapper = new RequestWrapper<>(request);
        if (workQueue.offer(requestWrapper, timeout, unit)) {
            checkAndStart();
            return requestWrapper.getFuture();
        }
        CompletableFuture<S> completableFuture = new CompletableFuture<>();
        completableFuture.complete(null);
        return completableFuture;
    }

    public void checkAndStart() {
        if (!isRunning() && running.compareAndSet(false, true)) {
            // 此时前一个while死循环可能还没死好, 所以本次while采用Block策略
            executorService.submit(this::run);
        }
    }

    public void run() {
        running.set(true);
        while (running.get()) {
            Optional<RequestWrapper<R, S>> item = pollTimeout();
            // 30秒还没获取到请请求，shutdown
            item.ifPresentOrElse(requestWrapper -> {
                try {
                    S response = handler.apply(requestWrapper.getRequest());
                    requestWrapper.complete(response);
                } catch (Exception e) {
                    log.error("thread is InterruptedException -> ", e);
                    requestWrapper.completeExceptionally(e);
                }
            }, this::stoppingPoll);
        }
    }

    private Optional<RequestWrapper<R, S>> pollTimeout() {
        try {
            return Optional.ofNullable(workQueue.poll(10000, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("thread in Queue is InterruptedException -> ", e);
            return Optional.empty();
        }
    }

    public void stoppingPoll() {
        running.set(false);
    }

    public boolean isRunning() {
        return this.running.get();
    }


    @Getter
    static class RequestWrapper<R, S> {
        private final CompletableFuture<S> future;
        private final R request;

        public RequestWrapper(R request) {
            this.request = request;
            this.future = new CompletableFuture<>();
        }

        public void complete(S response) {
            future.completeAsync(() -> response);
        }

        public void completeExceptionally(Exception e) {
            log.error("completeExceptionally -> ", e);
            getFuture().completeExceptionally(e);
        }
    }
}
