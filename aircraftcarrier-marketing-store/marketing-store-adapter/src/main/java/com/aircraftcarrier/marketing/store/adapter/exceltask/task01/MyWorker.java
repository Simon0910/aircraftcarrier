package com.aircraftcarrier.marketing.store.adapter.exceltask.task01;

import com.aircraftcarrier.framework.exceltask.Worker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

/**
 * @author zhipengliu
 */
@Component
@Slf4j
public class MyWorker implements Worker<MyExcelData> {

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
    }

}
