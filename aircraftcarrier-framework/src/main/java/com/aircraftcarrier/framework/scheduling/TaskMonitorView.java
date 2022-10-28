package com.aircraftcarrier.framework.scheduling;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author lzp
 */
public class TaskMonitorView implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private String taskName;
    private String cron;
    private String state;
    private int progress;

    private LocalDateTime nextTime;

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getNextTime() {
        if (nextTime == null) {
            return "";
        }
        return nextTime.format(FORMATTER);
    }

    public void setNextTime(LocalDateTime nextTime) {
        this.nextTime = nextTime;
    }


}
