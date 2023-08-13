package com.aircraftcarrier.marketing.store.adapter.exceltask;

import com.aircraftcarrier.marketing.store.adapter.exceltask.task01.MyExcelTask;
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
    private MyExcelTask myExcelTask;


    @GetMapping("/start")
    public String start(String fileName) {
        // 可以使用对象存储获取数据流
        if (StringUtils.hasText(fileName)) {
            myExcelTask.config().setExcelFileClassPath("files/" + fileName + ".xlsx");
        }
        return myExcelTask.start();
    }

    @GetMapping("/stop")
    public String stop() {
        return myExcelTask.stop();
    }

    @GetMapping("/reset")
    public String reset() {
        return myExcelTask.reset();
    }


    @GetMapping("/resetSuccessSheetRow")
    public String resetSuccessSheetRow(String maxSuccessSheetRow) throws IOException {
        return myExcelTask.resetSuccessSheetRow(maxSuccessSheetRow);
    }

    @GetMapping("/settingFromWithEnd")
    public String settingFromWithEnd(String fromSheetRow, String endSheetRow) {
        return myExcelTask.settingFromWithEnd(fromSheetRow, endSheetRow);
    }

}
