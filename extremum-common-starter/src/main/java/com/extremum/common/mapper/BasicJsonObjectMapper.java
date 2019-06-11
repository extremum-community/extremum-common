package com.extremum.common.mapper;

import com.extremum.common.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ofPattern;


/**
 * Created by vov4a on 08.10.15.
 */
public class BasicJsonObjectMapper extends ObjectMapper {

    private static final DateTimeFormatter FORMATTER = ofPattern(DateUtils.FORMAT);

    public BasicJsonObjectMapper() {
        // deserialization
        this.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true);
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

        this.registerModule(new BasicDeSerModule(this));
        this.registerModule(createJavaTimeModule());

        this.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        this.setDateFormat(DateUtils.DATE_FORMAT);
    }

    private JavaTimeModule createJavaTimeModule() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(ZonedDateTime.class, new ZoneDateTimeSerializer());
        javaTimeModule.addDeserializer(ZonedDateTime.class, new ZoneDateTimeDeserializer());
        return javaTimeModule;
    }

    private class ZoneDateTimeSerializer extends JsonSerializer<ZonedDateTime> {
        @Override
        public void serialize(ZonedDateTime zonedDateTime, JsonGenerator jsonGenerator,
                SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(zonedDateTime.format(FORMATTER));
        }
    }

    private class ZoneDateTimeDeserializer extends JsonDeserializer<ZonedDateTime> {
        @Override
        public ZonedDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
            return ZonedDateTime.parse(jsonParser.getValueAsString(), FORMATTER);
        }
    }

    @Override
    public ObjectMapper copy() {
        return new BasicJsonObjectMapper();
    }
}

