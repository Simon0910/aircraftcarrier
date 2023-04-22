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


    public <T extends AbstractUploadData> String start(Worker<T> worker, Class<T> modelClass) {
        log.info("snapshotPath 位置：{}", worker.config().getSnapshotPath());
        if (worker.isStarted()) {
            return "task is started !";
        }
        synchronized (this) {
            if (worker.isStarted()) {
                return "task is started !";
            }

            worker.setTaskThread(new Thread(() -> {
                try {
                    doRead(worker, modelClass);
                } catch (Exception e) {
                    log.error("doRead error: ", e);
                } finally {
                    worker.setStarted(false);
                }
            }));

            worker.setStopped(false);
            worker.setStarted(true);
            // 保证先 started = true 然后 started = false
            worker.start();
            return "start...";
        }
    }

    private <T extends AbstractUploadData> void doRead(Worker<T> worker, Class<T> modelClass) throws IOException {
        long start = System.currentTimeMillis();

        InputStream inputStream = getExcelFileInputStream(worker.config());
        UploadDataListener<T> listener = null;
        try {
            listener = new UploadDataListener<>(worker.config(), worker);
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


    public <T extends AbstractUploadData> String stop(Worker<T> worker) {
        if (worker.isStopped()) {
            return "task already stopped";
        }
        synchronized (this) {
            if (worker.isStopped()) {
                return "task already stopped";
            }

            if (worker.isStarted()) {
                worker.interrupt();
                worker.setStopped(true);
                return "stop...";
            }
            return "task not start";
        }

    }


    public <T extends AbstractUploadData> String reset(Worker<T> worker) {
        if (worker.isStarted()) {
            return "already started";
        }
        synchronized (this) {
            if (worker.isStarted()) {
                return "already started";
            }

            DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd_HH-mm-ss");
            DateTimeFormatter dateTimeFormatter = dateTimeFormatterBuilder.toFormatter();
            String nowDatetime = LocalDateTime.now().format(dateTimeFormatter) + "_";

            String successMapSnapshotFilePath = worker.config().getSuccessMapSnapshotFilePath();
            String newFilePath = successMapSnapshotFilePath.substring(0, successMapSnapshotFilePath.indexOf(TaskConfig.SUCCESS_MAP_FILENAME)) + nowDatetime + TaskConfig.SUCCESS_MAP_FILENAME;
            File file = new File(worker.config().getSuccessMapSnapshotFilePath());
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

            String errorMapSnapshotFilePath = worker.config().getErrorMapSnapshotFilePath();
            newFilePath = errorMapSnapshotFilePath.substring(0, errorMapSnapshotFilePath.indexOf(TaskConfig.ERROR_MAP_FILENAME)) + nowDatetime + TaskConfig.ERROR_MAP_FILENAME;
            file = new File(worker.config().getErrorMapSnapshotFilePath());
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


    public <T extends AbstractUploadData> String resetSuccessSheetRow(Worker<T> worker, String maxSuccessSheetRow) throws IOException {
        if (worker.isStarted()) {
            return "already started";
        }
        synchronized (this) {
            if (worker.isStarted()) {
                return "already started";
            }

            TaskConfig.checkSheetRow(maxSuccessSheetRow);

            worker.config().preCheckFile();

            try (BufferedWriter br = new BufferedWriter(new FileWriter(worker.config().getSuccessMapSnapshotFilePath()))) {
                br.write(maxSuccessSheetRow + TaskConfig.END);
                br.flush();
            }
            return "resetSuccessSheetRow ok";
        }
    }


    public <T extends AbstractUploadData> String settingFromWithEnd(Worker<T> worker, String fromSheetRow, String endSheetRow) {
        if (worker.isStarted()) {
            return "already started";
        }
        synchronized (this) {
            if (worker.isStarted()) {
                return "already started";
            }

            // set from
            worker.config().setFromSheetRowNo(fromSheetRow);
            worker.config().setEndSheetRowNo(endSheetRow);
            worker.config().doCheckConfig();
            return "settingFromAndEnd ok";
        }
    }


}
