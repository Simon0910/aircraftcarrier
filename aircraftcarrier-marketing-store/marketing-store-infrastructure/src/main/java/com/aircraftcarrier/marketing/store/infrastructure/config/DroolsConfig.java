package com.aircraftcarrier.marketing.store.infrastructure.config;

import com.aircraftcarrier.marketing.store.domain.drools.FileUtil;
import com.aircraftcarrier.marketing.store.domain.drools.KieUtils;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieContainer;
import org.kie.spring.KModuleBeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * @author lzp
 * 参考链接: https://reachmnadeem.wordpress.com/2021/05/09/getting-started-with-drools-in-spring-boot-project/
 */
@Configuration
public class DroolsConfig {

    public KieServices kieServices = KieUtils.getKieServices();

    @Bean
    public KieFileSystem kieFileSystem() throws IOException {
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        for (Resource fileResource : FileUtil.getFileResources()) {
            kieFileSystem.write(KieUtils.newClassPathResource(FileUtil.RULES_PATH + fileResource.getFilename()));
        }
        return kieFileSystem;
    }

    @Bean
    public KieContainer kieContainer(KieFileSystem kieFileSystem) {
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        return kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
    }

    @Bean
    public KModuleBeanFactoryPostProcessor kiePostProcessor() {
        return new KModuleBeanFactoryPostProcessor();
    }
}