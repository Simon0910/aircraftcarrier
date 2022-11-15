package com.aircraftcarrier.framework.web.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.util.Date;

/**
 * CustomDateSerializer
 *
 * @author liuzhipeng
 */
public class CustomDateSerializer extends JsonSerializer<Date> {

    DateTimeFormatter dateTimeFormatter;

    public CustomDateSerializer(String pattern) {
        this.dateTimeFormatter = DateTimeFormat.forPattern(pattern);
    }

    public CustomDateSerializer(DateTimeFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

    @Override
    public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        String dateStrFormat = dateTimeFormatter.print(new DateTime(date));
        jsonGenerator.writeString(dateStrFormat);
    }
}