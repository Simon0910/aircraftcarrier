package com.aircraftcarrier.framework.web.deser;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.util.Date;

/**
 * CustomDateSerializer
 *
 * @author liuzhipeng
 */
public class CustomDateDeserializer extends JsonDeserializer<Date> {

    DateTimeFormatter dateTimeFormatter;

    public CustomDateDeserializer(String pattern) {
        this.dateTimeFormatter = DateTimeFormat.forPattern(pattern);
    }

    public CustomDateDeserializer(DateTimeFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        String text = jsonParser.getText();
        LocalDateTime localDateTime = dateTimeFormatter.parseLocalDateTime(text);
        return localDateTime.toDate();
    }
}