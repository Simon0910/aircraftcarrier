package com.aircraftcarrier.framework.exceltask.refresh;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import com.aircraftcarrier.framework.exceltask.ExcelUtil;
import com.aircraftcarrier.framework.exceltask.Task;
import com.aircraftcarrier.framework.exceltask.TaskConfig;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

    // private BufferedWriter errorBufferedWriter;
    // https://cloud.tencent.com/developer/news/783592
    // private MappedByteBuffer successByteBuffer;

    private FileChannel successChannel;
    private ByteBuffer successBuffer;

    private FileChannel errorChannel;
    private long filePosition;
    private ByteBuffer errorBuffer;

    public LocalFileRefreshStrategy(TaskConfig config) {
        super(config);
    }


    public void preCheckFile() {
        try {
            Path path = Paths.get(config.getSuccessMapSnapshotFilePath());
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            if (!Files.exists(path)) {
                Files.createFile(path);
                log.info("文件创建成功: {}", path.toAbsolutePath());
            }

            path = Paths.get(config.getErrorMapSnapshotFilePath());
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            if (!Files.exists(path)) {
                Files.createFile(path);
                log.info("文件创建成功: {}", path.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("创建文件失败: " + e.getMessage());
        } catch (SecurityException e) {
            log.error("权限不足: " + e.getMessage());
        }
    }

    @Override
    public void preHandle(Task<?> task) throws IOException {
        String separator = "";
        if (!config.getSnapshotPath().endsWith(File.separator)) {
            separator = File.separator;
        }

        String fixPath = task.getClass().getSimpleName() + File.separator + "snapshot" + File.separator;
        config.setSuccessMapSnapshotFilePath(config.getSnapshotPath() + separator + fixPath + SUCCESS_MAP_FILENAME);
        config.setErrorMapSnapshotFilePath(config.getSnapshotPath() + separator + fixPath + ERROR_MAP_FILENAME);

        // check
        preCheckFile();

        Path path = Paths.get(config.getSuccessMapSnapshotFilePath());
        // 打开文件通道：写模式，不存在则创建
        this.successChannel = FileChannel.open(path,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
        );
        // size = （行数最大1048576（7个占位符）+ （sheet号,下划线2个字符和逗号））* 线程数
        int size = 10 * config.getThreadNum() + END.length();
        successBuffer = ByteBuffer.allocateDirect(size);

        path = Paths.get(config.getErrorMapSnapshotFilePath());
        // 打开文件通道：追加模式，不存在则创建
        this.errorChannel = FileChannel.open(path,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND // 追加模式
        );
        // 直接内存缓冲区（建议大小为页面大小的倍数，如4KB）
        this.errorBuffer = ByteBuffer.allocateDirect(4 * 1024); // 4KB缓冲区
        this.filePosition = errorChannel.size(); // 初始化写入位置
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

        String oldFilePath = config.getSuccessMapSnapshotFilePath();
        String newFilePath = oldFilePath.substring(0, oldFilePath.indexOf(SUCCESS_MAP_FILENAME)) + nowDatetime + SUCCESS_MAP_FILENAME;

        Path oldPath = Paths.get(oldFilePath);
        if (Files.exists(oldPath)) {
            try {
                FileUtils.moveFile(oldPath.toFile(), Paths.get(newFilePath).toFile());
            } catch (IOException e) {
                log.info("successMap reset失败: {}", oldPath.toAbsolutePath());
            }
        } else {
            log.info("successMap 不存在 {}", oldPath.toAbsolutePath());
        }

        oldFilePath = config.getErrorMapSnapshotFilePath();
        newFilePath = oldFilePath.substring(0, oldFilePath.indexOf(ERROR_MAP_FILENAME)) + nowDatetime + ERROR_MAP_FILENAME;

        oldPath = Paths.get(oldFilePath);
        if (Files.exists(oldPath)) {
            try {
                FileUtils.moveFile(oldPath.toFile(), Paths.get(newFilePath).toFile());
            } catch (IOException e) {
                log.info("errorMap reset失败: {}", oldPath.toAbsolutePath());
            }
        } else {
            log.info("errorMap 不存在 {}", oldPath.toAbsolutePath());
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
        if (!Files.exists(Paths.get(filePath))) {
            return null;
        }
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
        // successByteBuffer.position(0);
        // StringBuilder builder = new StringBuilder();
        // for (String sheetRow : successMap.values()) {
        //     builder.append(sheetRow).append(StrPool.COMMA);
        // }
        // builder.append(END);
        // successByteBuffer.put(builder.toString().getBytes());

        StringBuilder builder = new StringBuilder();
        successMap.forEach((key, value) -> builder.append(value).append(StrPool.COMMA));
        builder.append(END);

        successBuffer.clear();
        successBuffer.put(builder.toString().getBytes(StandardCharsets.UTF_8)); // 写入到缓冲区
        successBuffer.flip();                            // 切换为读模式
        successChannel.write(successBuffer, 0);  // 覆盖写入文件起始位置
        // successChannel.truncate(successBuffer.position()); // 清除旧数据的多余长度
        // successChannel.force(false);             // 强制刷盘保证数据持久化
    }

    @Override
    void doRefreshErrorMapSnapshot(Map<String, String> errorMap) throws Exception {
        // StringBuilder builder = new StringBuilder();
        // for (String sheetRow : errorMap.keySet()) {
        //     builder.append(sheetRow).append(StrPool.COMMA);
        // }
        // builder.append(StrPool.CRLF);
        // errorBufferedWriter.write(builder.toString());
        // errorBufferedWriter.flush();

        StringBuilder builder = new StringBuilder();
        errorMap.forEach((key, value) -> builder.append(key).append(StrPool.COMMA));
        builder.append(StrPool.CRLF);

        byte[] bytes = builder.toString().getBytes(StandardCharsets.UTF_8);
        if (errorBuffer.remaining() < bytes.length) {
            flushErrorBuffer(); // 缓冲区不足时刷盘
        }
        errorBuffer.put(bytes); // 写入到缓冲区
    }

    // 内部方法：刷缓冲区到磁盘
    private void flushErrorBuffer() throws IOException {
        errorBuffer.flip();
        int bytesWritten = errorChannel.write(errorBuffer, filePosition);
        filePosition += bytesWritten; // 更新写入位置
        errorBuffer.clear();
        // errorChannel.force(false); // 异步刷盘（可选）
    }

    @Override
    void close() throws Exception {
        // if (successByteBuffer != null) {
        //     successByteBuffer.force();
        //     successByteBuffer.clear();
        //     successByteBuffer = null;
        // }
        // if (errorBufferedWriter != null) {
        //     try {
        //         errorBufferedWriter.close();
        //         errorBufferedWriter = null;
        //     } catch (IOException ex) {
        //         errorBufferedWriter = null;
        //         log.error("close errorBufferedWriter error ", ex);
        //     }
        // }

        successChannel.force(true);

        if (errorBuffer.position() > 0) {
            flushErrorBuffer();
        }
        errorChannel.force(true);
    }

}
