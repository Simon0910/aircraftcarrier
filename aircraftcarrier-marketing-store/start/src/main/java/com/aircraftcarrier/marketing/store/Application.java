package com.aircraftcarrier.marketing.store;

import cn.easyes.starter.register.EsMapperScan;
import com.aircraftcarrier.framework.tookit.TimeLogUtil;
import com.aircraftcarrier.framework.web.config.SerializerConfiguration;
import com.aircraftcarrier.security.app.AuthServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot Starter
 *
 * @author admin
 */
@Slf4j
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@Import(SerializerConfiguration.class)
@PropertySource(value = {
        "project.properties",
        "jdbc.properties",
        "redis.properties"},
        encoding = "utf-8")
@SpringBootApplication(scanBasePackages = {"com.aircraftcarrier.marketing.store", "com.aircraftcarrier.security"}
        , scanBasePackageClasses = AuthServiceImpl.class
//        , exclude = {SecurityAutoConfiguration.class}
)
@MapperScan("com.aircraftcarrier.marketing.store.infrastructure.repository")
@EsMapperScan("com.aircraftcarrier.marketing.store.infrastructure.es")
public class Application {

    public static void main(String[] args) {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "100");

        log.info("Begin to start Spring Boot Application");
        long l = TimeLogUtil.startTime();

        SpringApplication.run(Application.class, args);

        TimeLogUtil.logElapsedTime("End starting Spring Boot Application", l);
    }
}
