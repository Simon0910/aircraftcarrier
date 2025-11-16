package com.aircraftcarrier.framework.tookit;

import com.cronutils.builder.CronBuilder;
import com.cronutils.converter.CronConverter;
import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.Test;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import static com.cronutils.model.field.expression.FieldExpressionFactory.*;

/**
 * CronUtilTest
 *
 * @author zhipengliu
 * @date 2025/11/16
 * @since 1.0
 */
public class CronUtilTest {

    /**
     * 定义
     */
    @Test
    public void definition() {
        // 创建不同格式的 Cron 定义
        CronDefinition unixCronDefinition =
                CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);

        CronDefinition quartzCronDefinition =
                CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);

        CronDefinition springCronDefinition =
                CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING);
    }

    /**
     * 解析器
     */
    @Test
    public void parser() {
        CronDefinition quartzCronDefinition =
                CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);

        // 创建解析器
        CronParser parser = new CronParser(quartzCronDefinition);

        // 解析 cron 表达式
        Cron quartzCron = parser.parse("0 0 12 * * ?");
    }


    /**
     * 模型
     */
    @Test
    public void model() {
        CronDefinition quartzCronDefinition =
                CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);

        // 创建解析器
        CronParser parser = new CronParser(quartzCronDefinition);

        // 解析 cron 表达式
        Cron quartzCron = parser.parse("0 0 12 * * ?");

        // 获取执行时间计算器
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCron);

        // 计算下一次执行时间
        ZonedDateTime now = ZonedDateTime.now();
        Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);
        nextExecution.ifPresent(time ->
                System.out.println("下一次执行: " +
                        time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
        );

        // 计算上一次执行时间
        Optional<ZonedDateTime> lastExecution = executionTime.lastExecution(now);
        lastExecution.ifPresent(time ->
                System.out.println("上一次执行: " +
                        time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
        );

        // 计算距离下一次执行的时间
        Optional<Duration> timeToNextExecution = executionTime.timeToNextExecution(now);

        // 计算距离上一次执行的时间
        Optional<Duration> timeFromLastExecution = executionTime.timeFromLastExecution(now);


    }


    /**
     * 表达式描述
     */
    @Test
    public void descriptor() {
        CronDefinition quartzCronDefinition =
                CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);

        // 创建解析器
        CronParser parser = new CronParser(quartzCronDefinition);

        // 解析 cron 表达式
        Cron quartzCron = parser.parse("0 0 12 * * ?");

        // 获取英文描述
        CronDescriptor descriptorEn = CronDescriptor.instance(Locale.US);
        String descriptionEn = descriptorEn.describe(quartzCron);
        System.out.println("英文描述: " + descriptionEn);

        // 获取中文描述
        CronDescriptor descriptorZh = CronDescriptor.instance(Locale.CHINA);
        String descriptionZh = descriptorZh.describe(quartzCron);
        System.out.println("中文描述: " + descriptionZh);
    }


    /**
     * 表达式生成
     */
    @Test
    public void builder() {
        // 获取中文描述
        CronDescriptor descriptorZh = CronDescriptor.instance(Locale.CHINA);


        CronDefinition cronDefinition =
                CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);

        // 构建 cron 表达式：每天中午12点执行
        Cron cron = CronBuilder.cron(cronDefinition)
                .withYear(always())
                .withDoM(always())
                .withMonth(always())
                .withDoW(questionMark())
                .withHour(on(12))
                .withMinute(on(0))
                .withSecond(on(0))
                .instance();

        System.out.println("生成的 cron 表达式: " + cron.asString());
        System.out.println("中文描述: " + descriptorZh.describe(cron));

        // 构建每5分钟执行一次的表达式
        Cron every5Minutes = CronBuilder.cron(cronDefinition)
                .withYear(always())
                .withDoM(always())
                .withMonth(always())
                .withDoW(questionMark())
                .withHour(always())
                .withMinute(every(5))
                .withSecond(on(0))
                .instance();

        System.out.println("每5分钟执行: " + every5Minutes.asString());
        System.out.println("中文描述: " + descriptorZh.describe(every5Minutes));

        // 构建工作日9点到17点每小时执行
        Cron workHours = CronBuilder.cron(cronDefinition)
                .withYear(always())
                .withDoM(questionMark())
                .withMonth(always())
                .withDoW(between(1, 5))  // 周一到周五
                .withHour(between(9, 17)) // 9点到17点
                .withMinute(on(0))
                .withSecond(on(0))
                .instance();

        System.out.println("工作时间执行: " + workHours.asString());
        System.out.println("中文描述: " + descriptorZh.describe(workHours));

    }

    /**
     * 不同 Cron 格式转换
     */
    @Test
    public void converter() {
        // Quartz 表达式转 Unix 表达式
        CronParser quartzParser = new CronParser(
                CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ)
        );

        Cron quartzCron = quartzParser.parse("0 0 12 * * ?");

        // 转换为 Unix 格式
//        String unixExpression = CronConverter.fromQuartzToUnix(quartzCron.asString());
//        System.out.println("Unix 格式: " + unixExpression);
//
//        // 转换为 Spring 格式
//        String springExpression = CronConverter.fromQuartzToSpring(quartzCron.asString());
//        System.out.println("Spring 格式: " + springExpression);
    }
}
