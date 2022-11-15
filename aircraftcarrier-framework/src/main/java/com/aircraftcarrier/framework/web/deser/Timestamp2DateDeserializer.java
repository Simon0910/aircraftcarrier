package com.aircraftcarrier.framework.web.deser;

import com.aircraftcarrier.framework.tookit.DateTimeUtil;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Date;

/**
 * CustomDateSerializer
 *
 * @author liuzhipeng
 */
public class Timestamp2DateDeserializer extends JsonDeserializer<Date> {

    @Override
    public Class<Date> handledType() {
        return Date.class;
    }

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        String text = jsonParser.getText();
        return DateTimeUtil.millisToDate(Long.valueOf(text));
    }
}
