package com.aircraftcarrier.framework.exceltask;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.exception.ExcelAnalysisStopException;
import com.alibaba.excel.read.metadata.ReadSheet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;


/**
 * @author zhipengliu
 */
@Slf4j
public class WorkTask {

    private final List<UploadDataListener<?>> listeners = new ArrayList<>();

    @PostConstruct
    public void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("shutdown hook, jvm runtime hook, listeners size {} ...", listeners.size());
            for (UploadDataListener<?> listener : listeners) {
                log.info("listener shutdown...");
                listener.shutdown();
            }
            log.info("shutdown hook, jvm runtime hook end.");
        }));
    }

    private InputStream getExcelFileInputStream(TaskConfig taskConfig) throws IOException {
        // 获取流
        File file = ResourceUtils.getFile("classpath:" + taskConfig.getExcelFileClassPath());
        log.info("excel位置：{}", file.toPath());
        return Files.newInputStream(file.toPath());
    }


    public <T extends AbstractUploadData> String start(Worker<T> worker, Class<T> modelClass, TaskConfig taskConfig) {
        log.info("snapshotPath 位置：{}", taskConfig.getSnapshotPath());
        if (taskConfig.isStarted()) {
            return "task is started !";
        }
        synchronized (this) {
            if (taskConfig.isStarted()) {
                return "task is started !";
            }

            taskConfig.setTaskThread(new Thread(() -> {
                try {
                    doRead(worker, modelClass, taskConfig);
                } catch (Exception e) {
                    log.error("doRead error: ", e);
                } finally {
                    taskConfig.setStarted(false);
                }
            }));

            taskConfig.setStopped(false);
            taskConfig.setStarted(true);
            // 保证先 started = true 然后 started = false
            taskConfig.getTaskThread().start();
            return "start...";
        }
    }

    private <T extends AbstractUploadData> void doRead(Worker<T> worker, Class<T> modelClass, TaskConfig taskConfig) throws IOException {
        long start = System.currentTimeMillis();

        InputStream inputStream = getExcelFileInputStream(taskConfig);
        UploadDataListener<T> listener = null;
        try {
            listener = new UploadDataListener<>(taskConfig, worker);
            listeners.add(listener);
            ExcelReader excelReader = EasyExcelFactory.read(inputStream, modelClass, listener).autoCloseStream(true).build();
            List<ReadSheet> readSheets = excelReader.excelExecutor().sheetList();
            excelReader.read(readSheets);
            excelReader.finish();
        } catch (Exception e) {
            if (e.getClass() != ExcelAnalysisStopException.class) {
                throw e;
            }
        } finally {
            if (listener != null) {
                listener.shutdown();
            }
            listeners.remove(listener);
        }

        log.info("doRead: {}", System.currentTimeMillis() - start);
    }


    public String stop(TaskConfig taskConfig) {
        if (taskConfig.isStopped()) {
            return "task already stopped";
        }
        synchronized (this) {
            if (taskConfig.isStopped()) {
                return "task already stopped";
            }

            if (taskConfig.getTaskThread() != null) {
                taskConfig.getTaskThread().interrupt();
                taskConfig.setStopped(true);
                return "stop...";
            }
            return "task not start";
        }

    }


    public String reset(TaskConfig taskConfig) {
        if (taskConfig.isStarted()) {
            return "already started";
        }
        synchronized (this) {
            if (taskConfig.isStarted()) {
                return "already started";
            }

            DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd_HH-mm-ss");
            DateTimeFormatter dateTimeFormatter = dateTimeFormatterBuilder.toFormatter();
            String nowDatetime = LocalDateTime.now().format(dateTimeFormatter) + "_";

            String successMapSnapshotFilePath = taskConfig.getSuccessMapSnapshotFilePath();
            String newFilePath = successMapSnapshotFilePath.substring(0, successMapSnapshotFilePath.indexOf(TaskConfig.SUCCESS_MAP_FILENAME)) + nowDatetime + TaskConfig.SUCCESS_MAP_FILENAME;
            File file = new File(taskConfig.getSuccessMapSnapshotFilePath());
            file.setReadable(true);
            file.setWritable(true);
            File newFile = new File(newFilePath);
            newFile.setReadable(true);
            // 新文件不存在，永远返回false
            // newFile.setWritable(true);
            if (file.renameTo(newFile)) {
                log.info("SuccessMap file renamed successfully.");
            } else {
                log.error("Failed to rename SuccessMap file.");
            }

            String errorMapSnapshotFilePath = taskConfig.getErrorMapSnapshotFilePath();
            newFilePath = errorMapSnapshotFilePath.substring(0, errorMapSnapshotFilePath.indexOf(TaskConfig.ERROR_MAP_FILENAME)) + nowDatetime + TaskConfig.ERROR_MAP_FILENAME;
            file = new File(taskConfig.getErrorMapSnapshotFilePath());
            file.setReadable(true);
            file.setWritable(true);
            newFile = new File(newFilePath);
            newFile.setReadable(true);
            // 新文件不存在，永远返回false
            // newFile.setWritable(true);
            if (file.renameTo(newFile)) {
                log.info("ErrorMap file renamed successfully.");
            } else {
                log.error("Failed to rename ErrorMap file.");
            }
            return "reset";
        }
    }


    public String resetSuccessSheetRow(TaskConfig taskConfig, String maxSuccessSheetRow) throws IOException {
        if (taskConfig.isStarted()) {
            return "already started";
        }
        synchronized (this) {
            if (taskConfig.isStarted()) {
                return "already started";
            }

            TaskConfig.checkSheetRow(maxSuccessSheetRow);

            taskConfig.preCheckFile();

            try (BufferedWriter br = new BufferedWriter(new FileWriter(taskConfig.getSuccessMapSnapshotFilePath()))) {
                br.write(maxSuccessSheetRow);
                br.flush();
            }
            return "resetSuccessSheetRow ok";
        }
    }


    public String settingFromWithEnd(TaskConfig taskConfig, String fromSheetRow, String endSheetRow) {
        if (taskConfig.isStarted()) {
            return "already started";
        }
        synchronized (this) {
            if (taskConfig.isStarted()) {
                return "already started";
            }

            // set from
            taskConfig.setFromSheetRowNo(fromSheetRow);
            taskConfig.setEndSheetRowNo(endSheetRow);
            taskConfig.doCheckConfig();
            return "settingFromAndEnd ok";
        }
    }


}
