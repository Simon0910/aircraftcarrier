package com.aircraftcarrier.framework.concurrent.basic.phaser;

import com.aircraftcarrier.framework.concurrent.BusyUtil;
import com.aircraftcarrier.framework.concurrent.MessageUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Phaser;

/**
 * 类注释内容
 *
 * @author zhipengliu
 * @date 2022/10/15
 * @since 1.0
 */
public class WorkWithPhaserSimilarCountDownLatch {
    public static void main(String[] args) {
        ExecutorService service = Executors.newFixedThreadPool(4);
//        ExecutorService service = ForkJoinPool.commonPool();

        Phaser phaser = new Phaser(3);

        // phaser 0
        MessageUtil.message("main 1 now is phaser {}", phaser.getPhase());
        service.submit(new DependentService(phaser, "111"));
        service.submit(new DependentService(phaser, "222"));
        service.submit(new DependentService(phaser, "333"));

        // wait phaser 0 finish
        phaser.awaitAdvance(phaser.getPhase());
        // wakeup phaser + 1

        // phaser 1
        MessageUtil.message("main 2 now is phaser {}", phaser.getPhase());
        service.submit(new DependentService(phaser, "ZhangSan"));
        service.submit(new DependentService(phaser, "Lisi"));
        service.submit(new DependentService(phaser, "WangWu"));

        // wait phaser 1 finish
        phaser.awaitAdvance(phaser.getPhase());
        // wakeup phaser + 1

        // phaser 2
        MessageUtil.message("main 3 now is phaser {}", phaser.getPhase());

        // shutdown
//        service.shutdownNow();
        service.shutdown();
    }

    public static class DependentService implements Runnable {
        private Phaser phaser;
        private String name;
        public DependentService(Phaser phaser, String name) {
            this.phaser = phaser;
            this.name = name;
        }

        @Override
        public void run() {
            MessageUtil.message("{} start up async task in phaser {}", name, phaser.getPhase());
            BusyUtil.busyFor(3);
            MessageUtil.message("{} ringing in phaser {}", name, phaser.getPhase());
            // similar to countDown()
            phaser.arrive();
            MessageUtil.message("{} end, now is phaser {}", name, phaser.getPhase());
        }
    }
}
