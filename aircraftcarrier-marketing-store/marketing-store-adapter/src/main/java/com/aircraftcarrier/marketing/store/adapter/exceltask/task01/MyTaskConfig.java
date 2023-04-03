package com.aircraftcarrier.marketing.store.adapter.exceltask.task01;

import com.aircraftcarrier.framework.exceltask.TaskConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author zhipengliu
 */
@Configuration
public class MyTaskConfig {
    @Value("${myExcelTask.switch.excelFileClassPath:}")
    private String excelFileClassPath;
    @Value("${myExcelTask.threadNum:1}")
    private int threadNum;
    @Value("${myExcelTask.poolName:default}")
    private String poolName;
    @Value("${myExcelTask.refresh.snapshot.period:1000}")
    private long refreshSnapshotPeriod;
    @Value("${myExcelTask.refresh.snapshot.path:./}")
    private String snapshotPath;
    @Value("${myExcelTask.batchSize:1}")
    private int batchSize;
    @Value("${myExcelTask.fromSheetRowNo:}")
    private String fromSheetRowNo;
    @Value("${myExcelTask.endSheetRowNo:}")
    private String endSheetRowNo;


    @Bean
    public TaskConfig myExcelTaskConfig(MyWorker myWorker) {
        return new TaskConfig.TaskConfigBuilder()
                .excelFileClassPath(excelFileClassPath)
                .threadNum(threadNum)
                .poolName(poolName)
                .refreshSnapshotPeriod(refreshSnapshotPeriod)
                .snapshotPathPath(snapshotPath)
                .batchSize(batchSize)
                .fromSheetRowNo(fromSheetRowNo)
                .endSheetRowNo(endSheetRowNo)
                .enableAbnormalAutoCheck(true)
                .abnormalSampleSize(200)
                .consecutiveAbnormalNum(100)
                .build(myWorker);
    }


}
