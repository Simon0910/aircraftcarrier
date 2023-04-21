package com.aircraftcarrier.framework.cache;

import jdk.internal.misc.Unsafe;

import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * <a href="https://github.com/dreamhead/patterns-of-distributed-systems/blob/master/content/singular-update-queue.md">...</a>
 *
 * @author zhipengliu
 */
public class SingularUpdateQueue<R, S> extends Thread {

    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long STATE = U.objectFieldOffset(SingularUpdateQueue.class, "state");
    private final ArrayBlockingQueue<RequestWrapper<R, S>> workQueue = new ArrayBlockingQueue<>(1000);
    private final Function<R, S> handler;
    private volatile int state;
    private volatile boolean isRunning = false;

    public SingularUpdateQueue(Function<R, S> handler) {
        this.handler = handler;
    }

    public CompletableFuture<S> submit(R request) throws InterruptedException {
        var requestWrapper = new RequestWrapper<R, S>(request);
        workQueue.put(requestWrapper);
        start();
        return requestWrapper.getFuture();
    }

    public CompletableFuture<S> submit(R request, long timeout, TimeUnit unit) throws InterruptedException {
        var requestWrapper = new RequestWrapper<R, S>(request);
        boolean offer = workQueue.offer(requestWrapper, timeout, unit);
        if (offer) {
            start();
            return requestWrapper.getFuture();
        }
        CompletableFuture<S> completableFuture = new CompletableFuture<>();
        completableFuture.complete(null);
        return completableFuture;
    }

    @Override
    public void start() {
        if (!isRunning() && compareAndSetState(0, 1)) {
            super.start();
        }
    }

    @Override
    public void run() {
        isRunning = true;
        while (isRunning) {
            Optional<RequestWrapper<R, S>> item = take();
            // 30秒还没获取到请请求，shutdown
            item.ifPresentOrElse(requestWrapper -> {
                try {
                    S response = handler.apply(requestWrapper.getRequest());
                    requestWrapper.complete(response);
                } catch (Exception e) {
                    requestWrapper.completeExceptionally(e);
                }
            }, this::shutdown);
        }
    }

    private Optional<RequestWrapper<R, S>> take() {
        try {
            return Optional.ofNullable(workQueue.poll(10000, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    public void shutdown() {
        this.state = 0;
        this.isRunning = false;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    private boolean compareAndSetState(int expect, int update) {
        return U.compareAndSetInt(this, STATE, expect, update);
    }

    static class RequestWrapper<R, S> {
        private final CompletableFuture<S> future;
        private final R request;

        public RequestWrapper(R request) {
            this.request = request;
            this.future = new CompletableFuture<>();
        }

        public CompletableFuture<S> getFuture() {
            return future;
        }

        public R getRequest() {
            return request;
        }

        public void complete(S response) {
            future.completeAsync(() -> response);
        }

        public void completeExceptionally(Exception e) {
            e.printStackTrace();
            getFuture().completeExceptionally(e);
        }
    }
}
