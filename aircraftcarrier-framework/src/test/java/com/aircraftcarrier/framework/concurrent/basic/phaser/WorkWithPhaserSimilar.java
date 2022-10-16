package com.aircraftcarrier.framework.concurrent.basic.phaser;

import com.aircraftcarrier.framework.concurrent.BusyUtil;
import com.aircraftcarrier.framework.concurrent.MessageUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

/**
 * <a href="https://www.youtube.com/watch?v=Xro4KwoMNJ8&list=RDLVXro4KwoMNJ8&start_radio=1&rv=Xro4KwoMNJ8&t=885">...</a>
 * Similar = CyclicBarrier + CountDownLatch
 *
 * @author zhipengliu
 * @date 2022/10/15
 * @since 1.0
 */
public class WorkWithPhaserSimilar {
    public static void main(String[] args) {
        ExecutorService service = Executors.newFixedThreadPool(4);

        Phaser phaser = new Phaser(1); // self-register

        // phaser 0
        MessageUtil.message("main 1 now is phaser {}", phaser.getPhase());
        service.submit(new Service(phaser, "111"));

        // wait phaser 0 finish
        phaser.arriveAndAwaitAdvance();
        // phaser++ = 1

        // phaser 1
        MessageUtil.message("main 2 now is phaser {}", phaser.getPhase());
        service.submit(new Service(phaser, "222"));
        service.submit(new Service(phaser, "333"));

        // wait phaser 1 finish
        phaser.arriveAndAwaitAdvance();
        // phaser++ = 2

        // phaser 2
        MessageUtil.message("main 3 now is phaser {}", phaser.getPhase());
        service.submit(new Service(phaser, "ZhangSan"));
        service.submit(new Service(phaser, "Lisi"));
        service.submit(new Service(phaser, "WangWu"));

        // wait phaser 2 finish
        phaser.arriveAndAwaitAdvance();
        // phaser++ = 3

        // shutdown
//        service.shutdownNow();
        service.shutdown();
    }

    public static class Service implements Runnable {
        private Phaser phaser;
        private String name;

        public Service(Phaser phaser, String name) {
            this.phaser = phaser;
            this.name = name;
            phaser.register();
        }

        @Override
        public void run() {
            try {
                MessageUtil.message("{} start up async task in phaser {}", name, phaser.getPhase());
                BusyUtil.busyFor(3);
                MessageUtil.message("{} ringing in phaser {}", name, phaser.getPhase());
                // similar to countDown()
                phaser.arrive();
                MessageUtil.message("{} end, now is phaser {}", name, phaser.getPhase());
            } finally {
                phaser.arriveAndDeregister();
            }

        }
    }
}
