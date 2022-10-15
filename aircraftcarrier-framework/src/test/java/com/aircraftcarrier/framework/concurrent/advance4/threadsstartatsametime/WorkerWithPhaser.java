package com.aircraftcarrier.framework.concurrent.advance4.threadsstartatsametime;

import java.time.Instant;
import java.util.concurrent.Phaser;

public class WorkerWithPhaser extends Thread {
    private final Phaser phaser;

    public WorkerWithPhaser(String name, Phaser phaser) {
        this.phaser = phaser;
        phaser.register();
        setName(name);
    }

    @Override
    public void run() {
        try {
            System.out.printf("[ %s ] created, blocked by the phaser\n", getName());
            phaser.arriveAndAwaitAdvance();
            System.out.printf("[ %s ] starts at: %s\n", getName(), Instant.now());
            // do actual work here...
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }
}
