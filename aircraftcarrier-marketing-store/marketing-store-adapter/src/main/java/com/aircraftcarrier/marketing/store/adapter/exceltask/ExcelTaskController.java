package com.aircraftcarrier.marketing.store.adapter.exceltask;

import com.aircraftcarrier.framework.exceltask.WorkTask;
import com.aircraftcarrier.marketing.store.adapter.exceltask.task01.MyExcelData;
import com.aircraftcarrier.marketing.store.adapter.exceltask.task01.MyWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author zhipengliu
 */
@Slf4j
@RestController
@RequestMapping("/readTask")
public class ExcelTaskController {

    @Resource
    private WorkTask task;
    @Resource
    private MyWorker myWorker;


    @GetMapping("/start")
    public String start(String fileName) {
        // 可以使用对象存储获取数据流
        if (StringUtils.hasText(fileName)) {
            myWorker.config().setExcelFileClassPath("files/" + fileName + ".xlsx");
        }
        return task.start(myWorker, MyExcelData.class);
    }

    @GetMapping("/stop")
    public String stop() {
        return task.stop(myWorker);
    }

    @GetMapping("/reset")
    public String reset() {
        return task.reset(myWorker);
    }


    @GetMapping("/resetSuccessSheetRow")
    public String resetSuccessSheetRow(String maxSuccessSheetRow) throws IOException {
        return task.resetSuccessSheetRow(myWorker, maxSuccessSheetRow);
    }

    @GetMapping("/settingFromWithEnd")
    public String settingFromWithEnd(String fromSheetRow, String endSheetRow) {
        return task.settingFromWithEnd(myWorker, fromSheetRow, endSheetRow);
    }

}
