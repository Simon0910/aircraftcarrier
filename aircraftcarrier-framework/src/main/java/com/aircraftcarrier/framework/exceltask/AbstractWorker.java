package com.aircraftcarrier.framework.exceltask;

/**
 * AbstractWorker
 *
 * @author zhipengliu
 * @date 2023/4/20
 * @since 1.0
 */
public abstract class AbstractWorker<T> implements Worker<T> {

    boolean started = false;
    boolean stopped = false;

    Thread taskThread;

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public void setStarted(boolean started) {
        this.started = started;
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    @Override
    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    @Override
    public void setTaskThread(Thread taskThread) {
        this.taskThread = taskThread;
    }

    @Override
    public boolean isAlive() {
        return taskThread.isAlive();
    }

    @Override
    public boolean isInterrupted() {
        return taskThread.isInterrupted();
    }

    @Override
    public void interrupt() {
        taskThread.interrupt();
    }

    @Override
    public void start() {
        taskThread.start();
    }
}
