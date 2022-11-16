package com.aircraftcarrier.framework.web.config;

import com.aircraftcarrier.framework.tookit.DateTimeUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author lzp
 */
@Configuration
public class SerializerConfiguration {

    @Value("${spring.jackson.date-format:yyyy-MM-dd HH:mm:ss}")
    private String pattern;

    /**
     * 用于转换RequestParam和PathVariable参数
     * string ==> LocalDate
     */
    @Bean
    public Converter<String, LocalDate> localDateConverter() {
        return new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(String source) {
                if (source.trim().length() == 0) {
                    return null;
                }
                try {
                    return LocalDate.parse(source);
                } catch (Exception e) {
                    return LocalDate.parse(source, DateTimeFormatter.ofPattern(DateTimeUtil.DATE_FORMAT));
                }
            }
        };
    }

    /**
     * 用于转换RequestParam和PathVariable参数
     * string ==> LocalDateTime
     */
    @Bean
    public Converter<String, LocalDateTime> localDateTimeConverter() {
        return new Converter<String, LocalDateTime>() {
            @Override
            public LocalDateTime convert(String source) {
                if (source.trim().length() == 0) {
                    return null;
                }
                // 先尝试ISO格式: 2019-07-15T16:00:00
                try {
                    return LocalDateTime.parse(source);
                } catch (Exception e) {
                    return LocalDateTime.parse(source, DateTimeFormatter.ofPattern(DateTimeUtil.STANDARD_FORMAT));
                }
            }
        };
    }


    /**
     * Json序列化和反序列化转换器，用于转换Post请求体中的json以及将我们的对象序列化为返回响应的json
     * {@link com.aircraftcarrier.marketing.store.adapter.config.WebMvcConfiguration#extendMessageConverters(java.util.List) }
     */
//    @Bean
//    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
//        JavaTimeModule javaTimeModule = new JavaTimeModule();
//        LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DateTimeUtil.STANDARD_FORMAT));
//        javaTimeModule.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);
//        return builder -> {
//            builder.simpleDateFormat(DateTimeUtil.STANDARD_FORMAT);
//            builder.serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern(DateTimeUtil.DATE_FORMAT)));
//            builder.serializers(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DateTimeUtil.STANDARD_FORMAT)));
//            // 序列化
//            builder.serializers(new DateSerializer(DateTimeFormat.forPattern(pattern)));
//            // 反序列化
//            builder.deserializers(new DateDeserializer(DateTimeFormat.forPattern(pattern)));
//            builder.modules(javaTimeModule);
//        };
//    }


    /**
     * Json序列化和反序列化转换器，用于转换Post请求体中的json以及将我们的对象序列化为返回响应的json
     * {@link com.aircraftcarrier.marketing.store.adapter.config.WebMvcConfiguration#extendMessageConverters(java.util.List) }
     */
//    @Bean
//    public ObjectMapper objectMapper() {
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
//
//        //LocalDateTime系列序列化和反序列化模块，继承自jsr310，我们在这里修改了日期格式
//        JavaTimeModule javaTimeModule = new JavaTimeModule();
//        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DateTimeUtil.STANDARD_FORMAT)));
//        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DateTimeUtil.DATE_FORMAT)));
//        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DateTimeUtil.TIME_FORMAT)));
//        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DateTimeUtil.STANDARD_FORMAT)));
//        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DateTimeUtil.DATE_FORMAT)));
//        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DateTimeUtil.TIME_FORMAT)));
//
//
//        //Date序列化和反序列化
//        javaTimeModule.addSerializer(Date.class, new JsonSerializer<>() {
//            @Override
//            public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
//                SimpleDateFormat formatter = new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT);
//                String formattedDate = formatter.format(date);
//                jsonGenerator.writeString(formattedDate);
//            }
//        });
//        javaTimeModule.addDeserializer(Date.class, new JsonDeserializer<>() {
//            @Override
//            public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
//                SimpleDateFormat format = new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT);
//                String date = jsonParser.getText();
//                try {
//                    return format.parse(date);
//                } catch (ParseException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        });
//
//        objectMapper.registerModule(javaTimeModule);
//        return objectMapper;
//    }

}
