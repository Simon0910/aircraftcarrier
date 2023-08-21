package com.aircraftcarrier.framework.exceltask;

import com.aircraftcarrier.framework.support.ApplicationContextClosedEvent;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.exception.ExcelAnalysisStopException;
import com.alibaba.excel.read.metadata.ReadSheet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;

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
 * <a href="https://blog.csdn.net/qq271859852/article/details/107442161">...</a>
 *
 * @author zhipengliu
 */
@Slf4j
public class TaskExecutor implements ApplicationContextClosedEvent {

    private static final List<UploadDataListener<?>> listeners = new ArrayList<>();

    private InputStream getExcelFileInputStream(TaskConfig taskConfig) throws IOException {
        // 获取流
        File file = ResourceUtils.getFile("classpath:" + taskConfig.getExcelFileClassPath());
        log.info("excel位置：{}", file.toPath());
        return Files.newInputStream(file.toPath());
    }


    public <T extends AbstractUploadData> String start(Task<T> task, Class<T> modelClass) {
        if (task.isStarted()) {
            return "task is started !";
        }
        synchronized (this) {
            if (task.isStarted()) {
                return "task is started !";
            }

            task.setTaskThread(new Thread(() -> {
                try {
                    doRead(task, modelClass);
                } catch (Exception e) {
                    log.error("doRead error: ", e);
                } finally {
                    task.setStarted(false);
                }
            }));

            task.setStopped(false);
            task.setStarted(true);
            // 保证先 started = true 然后 started = false
            task.doStart();
            return "start...";
        }
    }

    private <T extends AbstractUploadData> void doRead(Task<T> task, Class<T> modelClass) throws IOException {
        long start = System.currentTimeMillis();

        InputStream in = getExcelFileInputStream(task.config());
        UploadDataListener<T> listener = null;
        try {
            listener = new UploadDataListener<>(task.config(), task);
            listeners.add(listener);
            ExcelReader excelReader = EasyExcelFactory.read(in, modelClass, listener).autoCloseStream(true).build();
            List<ReadSheet> readSheets = excelReader.excelExecutor().sheetList();
            excelReader.read(readSheets);
            excelReader.finish();
        } catch (Exception e) {
            if (e.getClass() != ExcelAnalysisStopException.class) {
                throw e;
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (listener != null) {
                listener.shutdown();
            }
            listeners.remove(listener);
        }

        log.info("doRead elapsed time: {} ms.", System.currentTimeMillis() - start);
    }


    public <T extends AbstractUploadData> String stop(Task<T> task) {
        if (task.isStopped()) {
            return "task already stopped";
        }
        synchronized (this) {
            if (task.isStopped()) {
                return "task already stopped";
            }

            if (task.isStarted()) {
                task.interrupt();
                task.setStopped(true);
                return "stop...";
            }
            return "task not start";
        }

    }


    public <T extends AbstractUploadData> String reset(Task<T> task) {
        if (task.isStarted()) {
            return "already started";
        }
        synchronized (this) {
            if (task.isStarted()) {
                return "already started";
            }

            DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd_HH-mm-ss");
            DateTimeFormatter dateTimeFormatter = dateTimeFormatterBuilder.toFormatter();
            String nowDatetime = LocalDateTime.now().format(dateTimeFormatter) + "_";

            String successMapSnapshotFilePath = task.config().getSuccessMapSnapshotFilePath();
            String newFilePath = successMapSnapshotFilePath.substring(0, successMapSnapshotFilePath.indexOf(TaskConfig.SUCCESS_MAP_FILENAME)) + nowDatetime + TaskConfig.SUCCESS_MAP_FILENAME;
            File file = new File(task.config().getSuccessMapSnapshotFilePath());
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

            String errorMapSnapshotFilePath = task.config().getErrorMapSnapshotFilePath();
            newFilePath = errorMapSnapshotFilePath.substring(0, errorMapSnapshotFilePath.indexOf(TaskConfig.ERROR_MAP_FILENAME)) + nowDatetime + TaskConfig.ERROR_MAP_FILENAME;
            file = new File(task.config().getErrorMapSnapshotFilePath());
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


    public <T extends AbstractUploadData> String resetSuccessSheetRow(Task<T> task, String maxSuccessSheetRow) throws IOException {
        if (task.isStarted()) {
            return "already started";
        }
        synchronized (this) {
            if (task.isStarted()) {
                return "already started";
            }

            TaskConfig.checkSheetRow(maxSuccessSheetRow);

            task.config().preCheckFile();

            try (BufferedWriter br = new BufferedWriter(new FileWriter(task.config().getSuccessMapSnapshotFilePath()))) {
                br.write(maxSuccessSheetRow + TaskConfig.END);
                br.flush();
            }
            return "resetSuccessSheetRow ok";
        }
    }


    public <T extends AbstractUploadData> String settingFromWithEnd(Task<T> task, String fromSheetRow, String endSheetRow) {
        if (task.isStarted()) {
            return "already started";
        }
        synchronized (this) {
            if (task.isStarted()) {
                return "already started";
            }

            // set from
            task.config().setFromSheetRowNo(fromSheetRow);
            task.config().setEndSheetRowNo(endSheetRow);
            task.config().doCheckConfig();
            return "settingFromAndEnd ok";
        }
    }


    /**
     * <a href="https://blog.csdn.net/qq271859852/article/details/107442161">...</a>
     */
    @Override
    public void contextClosed() {
        // https://blog.csdn.net/qq271859852/article/details/107442161
        log.info("shutdown hook, jvm runtime hook, listeners size {} ...", listeners.size());
        for (UploadDataListener<?> listener : listeners) {
            log.info("listener shutdown...");
            listener.shutdown();
        }
        log.info("shutdown hook, jvm runtime hook end.");
    }
}
