package com.aircraftcarrier.framework.exceltask;

import com.aircraftcarrier.framework.exceltask.refresh.InMemoryRefreshStrategy;
import com.aircraftcarrier.framework.exceltask.refresh.LocalFileRefreshStrategy;
import com.aircraftcarrier.framework.exceltask.refresh.NonRefreshStrategy;

import java.io.IOException;

/**
 * AbstractTask
 *
 * @author zhipengliu
 * @date 2023/4/20
 * @since 1.0
 */
public abstract class AbstractTask<T extends AbstractExcelRow> implements Task<T> {
    private TaskConfig config;
    private boolean started = false;
    private boolean stopped = false;

    private Thread taskThread;

    protected TaskConfig taskConfig() {
        return null;
    }

    @Override
    public final TaskConfig config() {
        // https://rules.sonarsource.com/java/RSPEC-2168
        TaskConfig localConfig = config;
        if (localConfig == null) {
            synchronized (this) {
                localConfig = config;
                if (localConfig == null) {
                    config = localConfig = taskConfig();
                    if (localConfig == null) {
                        config = localConfig = new TaskConfig.TaskConfigBuilder().build(this);
                    }
                }

                if (config.isEnableRefresh()) {
                    this.config.setRefreshStrategy(new LocalFileRefreshStrategy(config));
                } else {
                    this.config.setRefreshStrategy(new NonRefreshStrategy(config));
                }
                try {
                    this.config.getRefreshStrategy().preHandle(this);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return localConfig;
    }

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
    public void doStart() {
        taskThread.start();
    }
}
