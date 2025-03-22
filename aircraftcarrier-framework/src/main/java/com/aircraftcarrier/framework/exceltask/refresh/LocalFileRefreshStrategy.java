package com.aircraftcarrier.framework.exceltask.refresh;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import com.aircraftcarrier.framework.exceltask.ExcelUtil;
import com.aircraftcarrier.framework.exceltask.Task;
import com.aircraftcarrier.framework.exceltask.TaskConfig;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author liuzhipeng
 * @since 2024/9/4
 */
@Slf4j
public class LocalFileRefreshStrategy extends AbstractRefreshStrategy {
    public static final String END = "$";
    public static final String SUCCESS_MAP_FILENAME = "successMap.log";
    public static final String ERROR_MAP_FILENAME = "errorMap.log";

    private BufferedWriter errorBufferedWriter;
    // https://cloud.tencent.com/developer/news/783592
    private MappedByteBuffer successByteBuffer;

    public LocalFileRefreshStrategy(TaskConfig config) {
        super(config);
    }


    public void preCheckFile() throws IOException {
        File file = new File(config.getSuccessMapSnapshotFilePath());
        File directory = file.getParentFile();
        if (!directory.exists()) {
            boolean mkdirs = directory.mkdirs();
            if (!mkdirs) {
                log.error("mkdirs error");
            }
        }
        if (!file.exists()) {
            boolean newFile = file.createNewFile();
            if (!newFile) {
                log.error("createNewFile error");
            }
        }

        // check
        file = new File(config.getErrorMapSnapshotFilePath());
        if (!file.exists()) {
            boolean newFile = file.createNewFile();
            if (!newFile) {
                log.error("createNewFile error");
            }
        }
    }

    @Override
    public void preHandle(Task<?> task) throws IOException {
        String separator = "/";
        String fixPath = task.getClass().getSimpleName() + "/snapshot/";
        if (config.getSnapshotPath().endsWith(separator)) {
            config.setSuccessMapSnapshotFilePath(config.getSnapshotPath() + fixPath + SUCCESS_MAP_FILENAME);
            config.setErrorMapSnapshotFilePath(config.getSnapshotPath() + fixPath + ERROR_MAP_FILENAME);
        } else {
            config.setSuccessMapSnapshotFilePath(config.getSnapshotPath() + separator + fixPath + SUCCESS_MAP_FILENAME);
            config.setErrorMapSnapshotFilePath(config.getSnapshotPath() + separator + fixPath + ERROR_MAP_FILENAME);
        }

        // check
        preCheckFile();

        // 失败文件通道
        errorBufferedWriter = new BufferedWriter(new FileWriter(config.getErrorMapSnapshotFilePath(), true));
        // 成功文件通道
        // 单个sheet 行数最大1048576（7个占位符） 列数最大16384
        // 7位 + 一个逗号 = 8位空
        int placeholderNum = 8;
        try (RandomAccessFile successRandomAccessFile = new RandomAccessFile(config.getSuccessMapSnapshotFilePath(), "rw")) {
            FileChannel fileChannel = successRandomAccessFile.getChannel();
            successByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, (long) config.getThreadNum() * placeholderNum + END.length());
        }
    }

    @Override
    public String loadSuccessMapSnapshot() throws IOException {
        String successStr = readFromFilePath(config.getSuccessMapSnapshotFilePath());
        if (CharSequenceUtil.isBlank(successStr)) {
            log.info("init - maxSuccessSnapshotPosition null");
            return null;
        }
        // 2_10,2_11,$ 写入 1_1000,1_1001,$ ===> 2_10,2_11,$01,$
        successStr = successStr.trim().substring(0, successStr.indexOf(END));
        Iterator<String> iterator = Splitter.on(StrPool.COMMA).omitEmptyStrings().trimResults().split(successStr).iterator();
        if (!iterator.hasNext()) {
            log.info("init - maxSuccessSnapshotPosition null");
            return null;
        }

        String maxSuccessSnapshotPosition = "0_0";
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (ExcelUtil.comparePosition(maxSuccessSnapshotPosition, next) < 0) {
                maxSuccessSnapshotPosition = next;
            }
        }
        log.info("init - maxSuccessSnapshotPosition {}", maxSuccessSnapshotPosition);
        return maxSuccessSnapshotPosition;
    }

    @Override
    public HashMap<String, String> loadErrorMapSnapshot() throws IOException {
        HashMap<String, String> errorMapSnapshot = new HashMap<>();
        String errorStr = readFromFilePath(config.getErrorMapSnapshotFilePath());
        if (CharSequenceUtil.isNotBlank(errorStr)) {
            for (String next : Splitter.on(StrPool.COMMA).omitEmptyStrings().trimResults().split(errorStr)) {
                errorMapSnapshot.put(next, CharSequenceUtil.EMPTY);
            }
        }
        return errorMapSnapshot;
    }

    @Override
    public void reset() {
        DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd_HH-mm-ss");
        DateTimeFormatter dateTimeFormatter = dateTimeFormatterBuilder.toFormatter();
        String nowDatetime = LocalDateTime.now().format(dateTimeFormatter) + "_";

        String successMapSnapshotFilePath = config.getSuccessMapSnapshotFilePath();
        String newFilePath = successMapSnapshotFilePath.substring(0, successMapSnapshotFilePath.indexOf(SUCCESS_MAP_FILENAME)) + nowDatetime + SUCCESS_MAP_FILENAME;
        File file = new File(config.getSuccessMapSnapshotFilePath());
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

        String errorMapSnapshotFilePath = config.getErrorMapSnapshotFilePath();
        newFilePath = errorMapSnapshotFilePath.substring(0, errorMapSnapshotFilePath.indexOf(ERROR_MAP_FILENAME)) + nowDatetime + ERROR_MAP_FILENAME;
        file = new File(config.getErrorMapSnapshotFilePath());
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
    }

    @Override
    public void resetSuccessSheetRow(String maxSuccessSheetRow) throws IOException {
        try (BufferedWriter br = new BufferedWriter(new FileWriter(config.getSuccessMapSnapshotFilePath()))) {
            br.write(maxSuccessSheetRow + END);
            br.flush();
        }
    }

    private String readFromFilePath(String filePath) throws IOException {
        // read
        StringBuilder builder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }


    @Override
    void doRefreshSuccessMapSnapshot(Map<String, String> successMap) throws Exception {
        successByteBuffer.position(0);
        StringBuilder builder = new StringBuilder();
        for (String sheetRow : successMap.values()) {
            builder.append(sheetRow).append(StrPool.COMMA);
        }
        builder.append(END);
        successByteBuffer.put(builder.toString().getBytes());
    }

    @Override
    void doRefreshErrorMapSnapshot(Map<String, String> errorMap) throws Exception {
        StringBuilder builder = new StringBuilder();
        for (String sheetRow : errorMap.keySet()) {
            builder.append(sheetRow).append(StrPool.COMMA);
        }
        builder.append(StrPool.CRLF);
        errorBufferedWriter.write(builder.toString());
        errorBufferedWriter.flush();
    }

    @Override
    void close() throws Exception {
        if (successByteBuffer != null) {
            successByteBuffer.force();
            successByteBuffer.clear();
            successByteBuffer = null;
        }
        if (errorBufferedWriter != null) {
            try {
                errorBufferedWriter.close();
            } catch (IOException ex) {
                errorBufferedWriter = null;
                log.error("close errorBufferedWriter error ", ex);
            }
        }
    }

}
