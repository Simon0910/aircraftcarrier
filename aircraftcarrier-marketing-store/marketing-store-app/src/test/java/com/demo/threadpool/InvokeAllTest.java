package com.demo.threadpool;
 
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
 
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
 
@Slf4j
public class InvokeAllTest {
 
    // https://bugs.openjdk.org/browse/JDK-8286463
    // DiscardPolicy may block invokeAll forever
    @Test
    public void test01() {

        AtomicInteger counter = new AtomicInteger(0);

        // a very small thread pool which is easy to fulfilled
        ExecutorService testPool = new ThreadPoolExecutor(
                1,
                1,
                1,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName("test-thread" + counter.getAndIncrement());
                    return thread;
                },
//                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardPolicy()
//                new ThreadPoolExecutor.DiscardOldestPolicy()
//                new ThreadPoolExecutor.AbortPolicy()
//                new ThreadPoolExecutor.CallerRunsPolicy()
        );
 
        // generate several jobs here
        final List<Callable<Integer>> tasks = IntStream.range(1, 10).mapToObj((i) -> new Callable<Integer>() {
            /**
             * Computes a result, or throws an exception if unable to do so.
             *
             * @return computed result
             * @throws Exception if unable to compute a result
             */
            @Override
            public Integer call() throws Exception {
                // sleep to simulate long-run job
                Thread.sleep(1000L);
                return i;
            }
        }).collect(Collectors.toList());

        try {
            testPool.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // you will never see this
        log.info("this executor can run through all tasks");

        testPool.shutdownNow();
//        testPool.shutdown();

        // you will never see this
        log.info("good bye");
    }
}