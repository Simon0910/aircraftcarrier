package com.aircraftcarrier.marketing.store.adapter.web;

import com.aircraftcarrier.framework.scheduling.DynamicTaskService;
import com.aircraftcarrier.marketing.store.adapter.scheduler.PrintTimeTask;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 定时任务
 *
 * @author lzp
 */
@Api(tags = "ScheduleController", produces = "application/json")
@Slf4j
@RequestMapping(value = "/schedule")
@RestController
public class ScheduleController {

    @Resource
    DynamicTaskService dynamicTaskService;

    @GetMapping("/register")
    public String register(String cron) {
        // "0/60 * * * * ?"
        for (int i = 0; i < 10; i++) {
            dynamicTaskService.register(new PrintTimeTask("task" + i, cron));
        }
        return "register";
    }

    @GetMapping("/cancel")
    public String cancel() {
        for (int i = 0; i < 1000; i++) {
            dynamicTaskService.cancel(new PrintTimeTask("task" + i, "null"));
        }
        return "cancel";
    }

    @GetMapping("/executeOnceManual")
    public String executeOnceManual() {
        for (int i = 0; i < 13; i++) {
            dynamicTaskService.executeOnceManual(new PrintTimeTask("task" + i, "null"));
        }
        return "executeOnceManual";
    }

    @GetMapping("/cancelManual")
    public String cancelManual() {
        for (int i = 0; i < 1000; i++) {
            dynamicTaskService.cancelManual(new PrintTimeTask("task" + i, "null"));
        }
        return "cancelManual";
    }

}
