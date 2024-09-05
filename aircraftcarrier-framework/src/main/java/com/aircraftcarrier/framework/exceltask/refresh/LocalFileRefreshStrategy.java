package com.aircraftcarrier.framework.exceltask.refresh;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import com.aircraftcarrier.framework.exceltask.ExcelUtil;
import com.aircraftcarrier.framework.exceltask.TaskConfig;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liuzhipeng
 * @since 2024/9/4
 */
@Slf4j
public class LocalFileRefreshStrategy extends AbstractRefreshStrategy {

    private BufferedWriter errorBufferedWriter;
    // https://cloud.tencent.com/developer/news/783592
    private MappedByteBuffer successByteBuffer;

    public LocalFileRefreshStrategy(TaskConfig config) {
        super(config);
    }

    @Override
    public void preHandle() throws IOException {
        // check
        config.preCheckFile();
        // 失败文件通道
        errorBufferedWriter = new BufferedWriter(new FileWriter(config.getErrorMapSnapshotFilePath(), true));
        // 成功文件通道
        // 单个sheet 行数最大1048576（7个占位符） 列数最大16384
        // 7位 + 一个逗号 = 8位空
        int placeholderNum = 8;
        try (RandomAccessFile successRandomAccessFile = new RandomAccessFile(config.getSuccessMapSnapshotFilePath(), "rw")) {
            FileChannel fileChannel = successRandomAccessFile.getChannel();
            successByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, (long) config.getThreadNum() * placeholderNum + TaskConfig.END.length());
        }
    }

    @Override
    public String loadSuccessMapSnapshot() throws IOException {
        String successStr = readFromFilePath(config.getSuccessMapSnapshotFilePath());
        String max = "0_0";
        String maxSuccessSnapshotPosition = null;
        if (CharSequenceUtil.isNotBlank(successStr)) {
            successStr = successStr.trim();
            // 2_10,2_11,$ 写入 1_1000,1_1001,$ ===> 2_10,2_11,$01,$
            successStr = successStr.substring(0, successStr.indexOf(TaskConfig.END));
            for (String next : Splitter.on(StrPool.COMMA).omitEmptyStrings().trimResults().split(successStr)) {
                if (ExcelUtil.comparePosition(max, next) < 0) {
                    max = maxSuccessSnapshotPosition = next;
                }
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
        builder.append(TaskConfig.END);
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
