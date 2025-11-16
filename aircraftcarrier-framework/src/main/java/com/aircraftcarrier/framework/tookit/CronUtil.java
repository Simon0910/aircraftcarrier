package com.aircraftcarrier.framework.tookit;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cron 工具类
 */
@Slf4j
public class CronUtil {

    private final CronParser parser;

    public CronUtil() {
        this.parser = new CronParser(
                CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ)
        );
    }

    /**
     * 验证 cron 表达式是否有效
     */
    public boolean isValidCronExpression(String expression) {
        try {
            parser.parse(expression);
            return true;
        } catch (IllegalArgumentException e) {
            log.error("Invalid cron expression: {}", expression, e);
            return false;
        }
    }

    /**
     * 获取下一次执行时间
     */
    public Optional<ZonedDateTime> getNextExecutionTime(String expression, ZonedDateTime from) {
        Cron cron = parser.parse(expression);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        return executionTime.nextExecution(from);
    }

    /**
     * 获取未来几次执行时间
     */
    public List<ZonedDateTime> getNextExecutionTimes(String expression,
                                                     ZonedDateTime from,
                                                     int count) {
        List<ZonedDateTime> executions = new ArrayList<>();

        Cron cron = parser.parse(expression);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);

        ZonedDateTime current = from;
        for (int i = 0; i < count; i++) {
            Optional<ZonedDateTime> next = executionTime.nextExecution(current);
            if (next.isPresent()) {
                executions.add(next.get());
                current = next.get();
            } else {
                break;
            }
        }

        return executions;
    }

    /**
     * 判断时间是否匹配 cron 表达式
     */
    public boolean isTimeMatching(String expression, ZonedDateTime time) {
        try {
            Cron cron = parser.parse(expression);
            ExecutionTime executionTime = ExecutionTime.forCron(cron);
            return executionTime.isMatch(time);
        } catch (IllegalArgumentException e) {
            log.error("Invalid cron expression: {}", expression, e);
            return false;
        }
    }
}