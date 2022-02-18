package com.aircraftcarrier.framework.dict.config;

import com.aircraftcarrier.framework.dict.core.service.DictDataFrameworkService;
import com.aircraftcarrier.framework.dict.core.util.DictFrameworkUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yudao
 */
@Configuration
public class DictAutoConfiguration {

    @Bean
    @ConditionalOnBean(DictDataFrameworkService.class)
    @SuppressWarnings("InstantiationOfUtilityClass")
    public DictFrameworkUtils dictUtils(DictDataFrameworkService service) {
        DictFrameworkUtils.init(service);
        return new DictFrameworkUtils();
    }

}
