package com.aircraftcarrier.framework.basic;

import java.util.concurrent.locks.StampedLock;

class Point {
    private final StampedLock sl = new StampedLock();
    private double x, y;

    // an exclusively locked method
    void move(double deltaX, double deltaY) {
        long stamp = sl.writeLock();
        try {
            x += deltaX;
            y += deltaY;
        } finally {
            sl.unlockWrite(stamp);
        }
    }

    // a read-only method
    // upgrade from optimistic read to read lock
    double distanceFromOrigin() {
        long stamp = sl.tryOptimisticRead();
        try {
            retryHoldingLock:
            for (; ; stamp = sl.readLock()) {
                if (stamp == 0L) continue retryHoldingLock;
                // possibly racy reads
                double currentX = x;
                double currentY = y;
                if (!sl.validate(stamp)) continue retryHoldingLock;
                return Math.hypot(currentX, currentY);
            }
        } finally {
            if (StampedLock.isReadLockStamp(stamp)) sl.unlockRead(stamp);
        }
    }

    // upgrade from optimistic read to write lock
    void moveIfAtOrigin(double newX, double newY) {
        long stamp = sl.tryOptimisticRead();
        try {
            retryHoldingLock:
            for (; ; stamp = sl.writeLock()) {
                if (stamp == 0L) continue retryHoldingLock;
                // possibly racy reads
                double currentX = x;
                double currentY = y;
                if (!sl.validate(stamp)) continue retryHoldingLock;
                if (currentX != 0.0 || currentY != 0.0) break;
                stamp = sl.tryConvertToWriteLock(stamp);
                if (stamp == 0L) continue retryHoldingLock;
                // exclusive access
                x = newX;
                y = newY;
                return;
            }
        } finally {
            if (StampedLock.isWriteLockStamp(stamp)) sl.unlockWrite(stamp);
        }
    }

    // upgrade read lock to write lock
    void moveIfAtOrigin2(double newX, double newY) {
        long stamp = sl.readLock();
        try {
            while (x == 0.0 && y == 0.0) {
                long ws = sl.tryConvertToWriteLock(stamp);
                if (ws != 0L) {
                    stamp = ws;
                    x = newX;
                    y = newY;
                    break;
                } else {
                    sl.unlockRead(stamp);
                    stamp = sl.writeLock();
                }
            }
        } finally {
            sl.unlock(stamp);
        }
    }
}