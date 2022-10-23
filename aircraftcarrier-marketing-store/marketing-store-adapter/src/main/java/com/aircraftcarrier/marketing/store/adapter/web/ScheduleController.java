package com.aircraftcarrier.marketing.store.adapter.web;

import com.aircraftcarrier.framework.scheduler.DynamicTaskService;
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
        dynamicTaskService.register(new PrintTimeTask(cron));
        return "register";
    }

    @GetMapping("/cancel")
    public String cancel() {
        dynamicTaskService.cancel(new PrintTimeTask());
        return "cancel";
    }

    @GetMapping("/executeOnceManual")
    public String executeOnceManual() {
        dynamicTaskService.executeOnceManual(new PrintTimeTask());
        return "executeOnceManual";
    }

    @GetMapping("/cancelManual")
    public String cancelManual() {
        dynamicTaskService.cancelManual(new PrintTimeTask());
        return "cancelManual";
    }

}
