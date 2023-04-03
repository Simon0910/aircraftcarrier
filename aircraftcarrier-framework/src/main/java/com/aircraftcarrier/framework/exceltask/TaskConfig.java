package com.aircraftcarrier.framework.exceltask;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;

/**
 * @author zhipengliu
 */
@Data
@Slf4j
public class TaskConfig {
    public static final String SUCCESS_MAP_FILENAME = "successMap.log";
    public static final String ERROR_MAP_FILENAME = "errorMap.log";

    private boolean started = false;
    private boolean stopped = false;

    private Thread taskThread;
    private int threadNum;
    private String poolName;
    private String snapshotPath;
    private long refreshSnapshotPeriod;
    private String excelFileClassPath;
    private int batchSize;
    private String fromSheetRowNo;
    private String endSheetRowNo;

    private String successMapSnapshotFilePath;
    private String errorMapSnapshotFilePath;


    private boolean enableAbnormalAutoCheck;
    private int consecutiveAbnormalNum;
    private int abnormalSampleSize;
    private int autoCheckForAbnormalPeriod;

    private TaskConfig() {
    }

    public static void checkSheetRow(String sheetRow) {
        if (CharSequenceUtil.isBlank(sheetRow)) {
            throw new ExcelTaskException("sheetRow is empty");
        }
        if (!sheetRow.contains(StrPool.UNDERLINE)) {
            throw new ExcelTaskException("sheetRow is not contains _");
        }

        String[] s = sheetRow.split(StrPool.UNDERLINE);
        if (s.length > 2) {
            throw new ExcelTaskException("sheetRow 格式错误");
        }
    }

    public String getSuccessMapSnapshotFilePath() {
        return successMapSnapshotFilePath;
    }

    public String getErrorMapSnapshotFilePath() {
        return errorMapSnapshotFilePath;
    }

    public void preCheckFile() throws IOException {
        File file = new File(getSuccessMapSnapshotFilePath());
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
        file = new File(getErrorMapSnapshotFilePath());
        if (!file.exists()) {
            boolean newFile = file.createNewFile();
            if (!newFile) {
                log.error("createNewFile error");
            }
        }
    }

    public void doCheckConfig() {
        Assert.isTrue(threadNum > 0, "threadNum must be > 0");
        Assert.isTrue(CharSequenceUtil.isNotBlank(poolName), "poolName must not be empty");
        Assert.isTrue(refreshSnapshotPeriod > 0, "refreshSnapshotPeriod must be > 0");
        Assert.isTrue(CharSequenceUtil.isNotBlank(snapshotPath), "snapshotPath must not be empty");
        Assert.isTrue(CharSequenceUtil.isNotBlank(excelFileClassPath), "excelFileClassPath must not be empty");
        Assert.isTrue(batchSize > 0, "batchSize must be > 0");
        Assert.isTrue(consecutiveAbnormalNum > 0, "consecutiveAbnormalNum must be > 0");
        Assert.isTrue(abnormalSampleSize > 0, "abnormalSampleSize must be > 0");
        Assert.isTrue(autoCheckForAbnormalPeriod > 0, "autoCheckForAbnormalPeriod must be > 0");
        Assert.isTrue(CharSequenceUtil.isNotBlank(successMapSnapshotFilePath), "successMapSnapshotFilePath must not be empty");
        Assert.isTrue(CharSequenceUtil.isNotBlank(errorMapSnapshotFilePath), "errorMapSnapshotFilePath must not be empty");
        if (CharSequenceUtil.isNotBlank(fromSheetRowNo)) {
            checkSheetRow(fromSheetRowNo);
        } else {
            fromSheetRowNo = null;
        }
        if (CharSequenceUtil.isNotBlank(endSheetRowNo)) {
            checkSheetRow(endSheetRowNo);
        } else {
            endSheetRowNo = null;
        }
    }

    public static class TaskConfigBuilder {
        private int threadNum = 1;
        private String poolName = "default";
        private long refreshSnapshotPeriod = 1000;
        private String snapshotPath = "./";

        private String excelFileClassPath;
        private int batchSize = 1;
        private String fromSheetRowNo;
        private String endSheetRowNo;

        private boolean enableAbnormalAutoCheck;
        private int consecutiveAbnormalNum = 100;
        private int abnormalSampleSize = 200;
        private int autoCheckForAbnormalPeriod = 1000;

        public TaskConfigBuilder excelFileClassPath(String excelFileClassPath) {
            this.excelFileClassPath = excelFileClassPath;
            return this;
        }

        public TaskConfigBuilder threadNum(int threadNum) {
            this.threadNum = threadNum;
            return this;
        }

        public TaskConfigBuilder poolName(String poolName) {
            this.poolName = poolName;
            return this;
        }

        public TaskConfigBuilder refreshSnapshotPeriod(long refreshSnapshotPeriod) {
            this.refreshSnapshotPeriod = refreshSnapshotPeriod;
            return this;
        }

        public TaskConfigBuilder snapshotPathPath(String snapshotPath) {
            this.snapshotPath = snapshotPath;
            return this;
        }

        public TaskConfigBuilder batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public TaskConfigBuilder fromSheetRowNo(String fromSheetRowNo) {
            this.fromSheetRowNo = fromSheetRowNo;
            return this;
        }

        public TaskConfigBuilder endSheetRowNo(String endSheetRowNo) {
            this.endSheetRowNo = endSheetRowNo;
            return this;
        }

        public TaskConfigBuilder enableAbnormalAutoCheck(boolean enableAbnormalAutoCheck) {
            this.enableAbnormalAutoCheck = enableAbnormalAutoCheck;
            return this;
        }

        public TaskConfigBuilder consecutiveAbnormalNum(int consecutiveAbnormalNum) {
            this.consecutiveAbnormalNum = consecutiveAbnormalNum;
            return this;
        }

        public TaskConfigBuilder abnormalSampleSize(int abnormalSampleSize) {
            this.abnormalSampleSize = abnormalSampleSize;
            return this;
        }

        public TaskConfigBuilder autoCheckForAbnormalPeriod(int autoCheckForAbnormalPeriod) {
            this.autoCheckForAbnormalPeriod = autoCheckForAbnormalPeriod;
            return this;
        }


        public TaskConfig build(Worker<?> worker) {
            TaskConfig config = new TaskConfig();

            config.setThreadNum(threadNum);
            config.setPoolName(poolName);
            config.setRefreshSnapshotPeriod(refreshSnapshotPeriod);
            config.setSnapshotPath(snapshotPath);
            config.setExcelFileClassPath(excelFileClassPath);
            config.setBatchSize(batchSize);
            config.setFromSheetRowNo(fromSheetRowNo);
            config.setEndSheetRowNo(endSheetRowNo);
            config.setEnableAbnormalAutoCheck(enableAbnormalAutoCheck);
            config.setConsecutiveAbnormalNum(consecutiveAbnormalNum);
            config.setAbnormalSampleSize(abnormalSampleSize);
            config.setAutoCheckForAbnormalPeriod(autoCheckForAbnormalPeriod);

            String separator = "/";
            String fixPath = worker.getClass().getSimpleName() + "/snapshot/";
            if (config.getSnapshotPath().endsWith(separator)) {
                config.setSuccessMapSnapshotFilePath(config.getSnapshotPath() + fixPath + SUCCESS_MAP_FILENAME);
                config.setErrorMapSnapshotFilePath(config.getSnapshotPath() + fixPath + ERROR_MAP_FILENAME);
            } else {
                config.setSuccessMapSnapshotFilePath(config.getSnapshotPath() + separator + fixPath + SUCCESS_MAP_FILENAME);
                config.setErrorMapSnapshotFilePath(config.getSnapshotPath() + separator + fixPath + ERROR_MAP_FILENAME);
            }

            config.doCheckConfig();
            log.info("snapshotPath 位置：{}", config.getSnapshotPath());
            return config;
        }
    }
}
