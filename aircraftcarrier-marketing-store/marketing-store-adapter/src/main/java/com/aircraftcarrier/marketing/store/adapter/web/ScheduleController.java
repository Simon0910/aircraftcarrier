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

    @GetMapping("/updateCron")
    public String add(String cron) {
        // "0/10 * * * * ?"
        dynamicTaskService.add(new PrintTimeTask(cron));
        return "ok";
    }

    @GetMapping("/updateTimer")
    public String stop() {
        dynamicTaskService.stop(new PrintTimeTask());
        return "ok";
    }


}
