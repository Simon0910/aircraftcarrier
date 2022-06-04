package com.aircraftcarrier.marketing.store;

import com.aircraftcarrier.framework.web.LocalDateTimeSerializerConfig;
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
        "project.properties",
        "jdbc.properties",
        "redis.properties"},
        encoding = "utf-8")
@SpringBootApplication(scanBasePackages = "com.aircraftcarrier.marketing.store"
//        , exclude = {SecurityAutoConfiguration.class}
)
@MapperScan("com.aircraftcarrier.marketing.store.infrastructure.repository")
public class Application {

    public static void main(String[] args) {
        log.info("Begin to start Spring Boot Application");
        long startTime = System.currentTimeMillis();

        SpringApplication.run(Application.class, args);

        long endTime = System.currentTimeMillis();
        log.info("End starting Spring Boot Application, Time used: " + (endTime - startTime));
    }
}
