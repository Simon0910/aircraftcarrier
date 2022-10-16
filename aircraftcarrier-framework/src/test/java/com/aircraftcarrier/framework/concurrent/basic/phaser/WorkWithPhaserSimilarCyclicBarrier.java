package com.aircraftcarrier.framework.concurrent.basic.phaser;

import com.aircraftcarrier.framework.concurrent.BusyUtil;
import com.aircraftcarrier.framework.concurrent.MessageUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

/**
 * 类注释内容
 *
 * @author zhipengliu
 * @date 2022/10/15
 * @since 1.0
 */
public class WorkWithPhaserSimilarCyclicBarrier {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(4);

        Phaser phaser = new Phaser(3);

        MessageUtil.message("main 1 now is phaser {}", phaser.getPhase());
        service.submit(new Task(phaser, "111"));
        service.submit(new Task(phaser, "222"));
        service.submit(new Task(phaser, "333"));

        MessageUtil.message("main 2 now is phaser {}", phaser.getPhase());
        service.submit(new Task(phaser, "ZhangSan"));
        service.submit(new Task(phaser, "Lisi"));
        service.submit(new Task(phaser, "WangWu"));

        MessageUtil.message("main 3 now is phaser {}", phaser.getPhase());

        // shutdown
//        service.shutdownNow();
        service.shutdown();
        service.awaitTermination(5, TimeUnit.DAYS);
    }

    public static class Task implements Runnable {
        private Phaser phaser;
        private String name;
        public Task(Phaser phaser, String name) {
            this.phaser = phaser;
            this.name = name;
        }

        @Override
        public void run() {
            MessageUtil.message("{} start up async task in phaser {}", name, phaser.getPhase());
            // similar to cyclicBarrier.await()
            phaser.arriveAndAwaitAdvance();
            // wakeup phaser + 1
            BusyUtil.busyFor(3);
            MessageUtil.message("{} ringing in phaser {}", name, phaser.getPhase());
            MessageUtil.message("{} end, now is phaser {}", name, phaser.getPhase());
        }
    }
}
