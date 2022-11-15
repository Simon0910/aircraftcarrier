package com.aircraftcarrier.marketing.store.adapter.config;

import com.aircraftcarrier.framework.web.deser.CustomDateDeserializer;
import com.aircraftcarrier.framework.web.ser.CustomDateSerializer;
import org.joda.time.format.DateTimeFormat;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

/**
 * description
 *
 * @author liuzhipeng
 */
@Configuration
public class JacksonConfig {

    private static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder
                // 反序列化
                .deserializerByType(Date.class, new CustomDateDeserializer(DateTimeFormat.forPattern(STANDARD_FORMAT)))
                // 序列化
                .serializerByType(Date.class, new CustomDateSerializer(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")));
    }


}
