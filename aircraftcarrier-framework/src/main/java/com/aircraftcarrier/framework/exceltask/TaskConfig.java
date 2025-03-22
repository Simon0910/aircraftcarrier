package com.aircraftcarrier.framework.exceltask;

import com.aircraftcarrier.framework.exceltask.refresh.RefreshStrategy;
import com.aircraftcarrier.framework.tookit.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * TaskConfig
 *
 * @author zhipengliu
 */
@Data
@Slf4j
public class TaskConfig {
    private int threadNum;
    private String poolName;
    private String excelFileClassPath;
    private int batchSize;
    private String fromSheetRowNo;
    private String endSheetRowNo;

    private boolean enableRefresh;
    private long refreshSnapshotPeriod;
    private RefreshStrategy refreshStrategy;

    // Local
    private String snapshotPath;
    private String successMapSnapshotFilePath;
    private String errorMapSnapshotFilePath;

    private boolean enableAbnormalAutoCheck;
    private int consecutiveAbnormalNum;
    private int abnormalSampleSize;
    private int autoCheckForAbnormalPeriod;

    private TaskConfig() {
    }

    public static class TaskConfigBuilder {
        private int threadNum = 1;
        private String poolName = "default-excel";
        private String excelFileClassPath;
        private int batchSize = 1;
        private String fromSheetRowNo;
        private String endSheetRowNo;

        private boolean enableRefresh;
        private long refreshSnapshotPeriod = 1000;

        // Local
        private String snapshotPath = "./";

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

        public TaskConfigBuilder enableRefresh(boolean enableRefresh) {
            this.enableRefresh = enableRefresh;
            return this;
        }

        public TaskConfigBuilder refreshSnapshotPeriod(long refreshSnapshotPeriod) {
            this.refreshSnapshotPeriod = refreshSnapshotPeriod;
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


        public TaskConfig build(Task<?> task) {
            TaskConfig config = new TaskConfig();

            config.setThreadNum(threadNum);
            config.setPoolName(poolName);
            config.setExcelFileClassPath(excelFileClassPath);
            config.setBatchSize(batchSize);

            config.setEnableRefresh(enableRefresh);
            config.setRefreshSnapshotPeriod(refreshSnapshotPeriod);
            config.setSnapshotPath(snapshotPath);

            config.setFromSheetRowNo(StringUtil.isBlank(fromSheetRowNo) ? null : fromSheetRowNo);
            config.setEndSheetRowNo(StringUtil.isBlank(endSheetRowNo) ? null : endSheetRowNo);

            config.setEnableAbnormalAutoCheck(enableAbnormalAutoCheck);
            config.setConsecutiveAbnormalNum(consecutiveAbnormalNum);
            config.setAbnormalSampleSize(abnormalSampleSize);
            config.setAutoCheckForAbnormalPeriod(autoCheckForAbnormalPeriod);
            log.info("snapshotPath 位置：{}", config.getSnapshotPath());
            return config;
        }
    }
}
