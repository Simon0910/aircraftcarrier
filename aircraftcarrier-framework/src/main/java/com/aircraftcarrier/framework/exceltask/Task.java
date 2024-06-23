package com.aircraftcarrier.framework.exceltask;


/**
 * Task
 *
 * @author zhipengliu
 */
public interface Task<T extends AbstractExcelRow> {

    TaskConfig config();

    boolean isStarted();

    void setStarted(boolean started);

    boolean isStopped();

    void setStopped(boolean stopped);

    void setTaskThread(Thread taskThread);

    boolean isAlive();

    boolean isInterrupted();

    void interrupt();

    void doStart();
}
