package com.aircraftcarrier.marketing.store.adapter.exceltask.task01;

import com.aircraftcarrier.framework.exceltask.AbstractWorker;
import com.aircraftcarrier.framework.exceltask.TaskConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

/**
 * @author zhipengliu
 */
@Component
@Slf4j
public class MyWorker extends AbstractWorker<MyExcelData> {
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

    @Override
    public TaskConfig taskConfig() {
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
                .build(this);
    }

    @Override
    public boolean check(MyExcelData myExcelData) {
        // trim fix filter
        return true;
    }

    /**
     * 处理存量数据
     *
     * @param threadBatchList threadBatchList
     */
    @Override
    public void doWorker(LinkedList<MyExcelData> threadBatchList) {
        MyExcelData first = threadBatchList.getFirst();
        MyExcelData last = threadBatchList.getLast();
        log.info("MyWorker [{} - {}] start", first.getRowNo(), last.getRowNo());
        for (MyExcelData myExcelData : threadBatchList) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
