package com.aircraftcarrier.framework.exceltask;

import cn.hutool.core.text.CharSequenceUtil;
import com.aircraftcarrier.framework.support.ApplicationContextClosedEvent;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.exception.ExcelAnalysisStopException;
import com.alibaba.excel.read.metadata.ReadSheet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;


/**
 * <a href="https://blog.csdn.net/qq271859852/article/details/107442161">...</a>
 *
 * @author zhipengliu
 */
@Slf4j
public class TaskExecutor implements ApplicationContextClosedEvent {

    private static final List<ExcelReadListener<?>> listeners = new ArrayList<>();

    private InputStream getExcelFileInputStream(TaskConfig taskConfig) throws IOException {
        // 获取流
        File file = ResourceUtils.getFile("classpath:" + taskConfig.getExcelFileClassPath());
        log.info("excel位置：{}", file.toPath());
        return Files.newInputStream(file.toPath());
    }


    public <T extends AbstractExcelRow> String start(Task<T> task, Class<T> modelClass) {
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

    private <T extends AbstractExcelRow> void doRead(Task<T> task, Class<T> modelClass) throws Exception {
        long start = System.currentTimeMillis();

        InputStream in = getExcelFileInputStream(task.config());
        ExcelReadListener<T> listener = null;
        try {
            listener = new ExcelReadListener<>(task, task.config());
            listeners.add(listener);
            ExcelReader excelReader = EasyExcelFactory.read(in, modelClass, listener).autoCloseStream(true).build();
            List<ReadSheet> readSheets = excelReader.excelExecutor().sheetList();
            excelReader.read(readSheets);
            excelReader.finish();
        } catch (Exception e) {
            // ExcelAnalysisStopException 会被忽略，所以不会被捕获
            // com.alibaba.excel.analysis.ExcelAnalyserImpl#analysis
            if (e.getClass() != ExcelAnalysisStopException.class) {
                throw e;
            }
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                log.error("close input stream error: ", e);
            }
            if (listener != null) {
                listener.shutdown();
            }
            listeners.remove(listener);
        }

        log.info("doRead elapsed time: {} ms.", System.currentTimeMillis() - start);
    }


    public <T extends AbstractExcelRow> String stop(Task<T> task) {
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


    public <T extends AbstractExcelRow> String reset(Task<T> task) {
        if (task.isStarted()) {
            return "already started";
        }
        synchronized (this) {
            if (task.isStarted()) {
                return "already started";
            }
            task.config().getRefreshStrategy().reset();
            return "reset";
        }
    }


    public <T extends AbstractExcelRow> String resetSuccessSheetRow(Task<T> task, String maxSuccessSheetRow) throws IOException {
        if (task.isStarted()) {
            return "already started";
        }
        synchronized (this) {
            if (task.isStarted()) {
                return "already started";
            }
            ExcelUtil.checkSheetRow(maxSuccessSheetRow);
            task.config().getRefreshStrategy().resetSuccessSheetRow(maxSuccessSheetRow);
            return "resetSuccessSheetRow ok";
        }
    }


    public <T extends AbstractExcelRow> String settingFromWithEnd(Task<T> task, String fromSheetRow, String endSheetRow) {
        if (task.isStarted()) {
            return "already started";
        }
        synchronized (this) {
            if (task.isStarted()) {
                return "already started";
            }
            // set from
            if (CharSequenceUtil.isNotBlank(fromSheetRow)) {
                ExcelUtil.checkSheetRow(fromSheetRow);
                task.config().setFromSheetRowNo(fromSheetRow);
            } else {
                task.config().setFromSheetRowNo(null);
            }
            if (CharSequenceUtil.isNotBlank(endSheetRow)) {
                ExcelUtil.checkSheetRow(endSheetRow);
                task.config().setEndSheetRowNo(endSheetRow);
            } else {
                task.config().setEndSheetRowNo(null);
            }
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
        for (ExcelReadListener<?> listener : listeners) {
            log.info("listener shutdown...");
            listener.shutdown();
        }
        log.info("shutdown hook, jvm runtime hook end.");
    }
}
