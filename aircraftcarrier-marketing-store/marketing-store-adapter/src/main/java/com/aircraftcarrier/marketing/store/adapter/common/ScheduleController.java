package com.aircraftcarrier.marketing.store.adapter.common;

import com.aircraftcarrier.framework.model.response.MultiResponse;
import com.aircraftcarrier.framework.scheduling.TaskMonitorView;
import com.aircraftcarrier.framework.scheduling.TaskService;
import com.aircraftcarrier.marketing.store.adapter.scheduler.PrintTimeTask;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

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
    TaskService dynamicTaskService;

    @GetMapping("/register")
    public String register(String cron) {
        // "0/60 * * * * ?"
        for (int i = 1; i <= 11; i++) {
            if (i == 11) {
                dynamicTaskService.register(new PrintTimeTask("task-" + i, "0/25 * * * * ?"));
                break;
            }
            dynamicTaskService.register(new PrintTimeTask("task-" + i, "0/20 * * * * ?"));
        }
        return "register";
    }

    @GetMapping("/cancel")
    public String cancel(String taskName) {
        dynamicTaskService.cancel(new PrintTimeTask(taskName, "null"));
        return "cancel";
    }

    @GetMapping("/executeOnceManual")
    public String executeOnceManual() {
        for (int i = 1; i <= 13; i++) {
            dynamicTaskService.executeOnceManual(new PrintTimeTask("task" + i, "null"));
        }
        return "executeOnceManual";
    }

    @GetMapping("/cancelManual")
    public String cancelManual(String taskName) {
        dynamicTaskService.cancelManual(new PrintTimeTask(taskName, "null"));
        return "cancelManual";
    }

    @GetMapping("/monitor")
    public MultiResponse<TaskMonitorView> monitor() {
        List<TaskMonitorView> taskList = dynamicTaskService.getTaskList();
        return MultiResponse.ok(taskList);
    }
}
