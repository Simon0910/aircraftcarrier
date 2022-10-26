package com.aircraftcarrier.bpm;

import com.aircraftcarrier.framework.tookit.TimeLogUtil;
import com.aircraftcarrier.framework.web.LocalDateTimeSerializerConfig;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

/**
 * Spring Boot Starter
 *
 * @author admin
 */
@Slf4j
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@Import(LocalDateTimeSerializerConfig.class)
@PropertySource(value = {
        "project.properties"
//        ,"jdbc.properties"
//        ,"redis.properties"
}, encoding = "utf-8")
@SpringBootApplication(scanBasePackages = {"com.aircraftcarrier.bpm", "com.aircraftcarrier.security"}
//        , exclude = {SecurityAutoConfiguration.class}
)
@MapperScan("com.aircraftcarrier.bpm.infrastructure.repository")
public class Application {

    public static void main(String[] args) {
        log.info("Begin to start Spring Boot Application");
        Stopwatch stopwatch = TimeLogUtil.startStopwatchTime();

        SpringApplication.run(Application.class, args);

        log.info("End starting Spring Boot Application, Time used: {}", TimeLogUtil.endStopwatchTime(stopwatch));
    }
}
