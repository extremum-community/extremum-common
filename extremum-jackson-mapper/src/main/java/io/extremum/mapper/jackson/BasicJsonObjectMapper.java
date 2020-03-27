package io.extremum.mapper.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.extremum.datetime.ApiDateTimeFormat;
import io.extremum.datetime.DateConstants;
import io.extremum.mapper.jackson.module.BasicSerializationDeserializationModule;
import io.extremum.mapper.jackson.module.StringOrObjectModule;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Locale;


/**
 * Created by vov4a on 08.10.15.
 */
public class BasicJsonObjectMapper extends ObjectMapper {

    private final ApiDateTimeFormat dateTimeFormat = new ApiDateTimeFormat();

    public BasicJsonObjectMapper() {
        this.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true);
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

        this.registerModule(new BasicSerializationDeserializationModule(this));
        this.registerModule(new StringOrObjectModule());
        this.registerModule(createJavaTimeModule());

        this.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        this.setDateFormat(new SimpleDateFormat(DateConstants.DATETIME_FORMAT_WITH_MICROS, Locale.US));
    }

    private JavaTimeModule createJavaTimeModule() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(ZonedDateTime.class, new ZoneDateTimeSerializer());
        javaTimeModule.addDeserializer(ZonedDateTime.class, new ZoneDateTimeDeserializer());
        return javaTimeModule;
    }

    private class ZoneDateTimeSerializer extends JsonSerializer<ZonedDateTime> {
        @Override
        public void serialize(ZonedDateTime dateTime, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(dateTimeFormat.format(dateTime));
        }
    }

    private class ZoneDateTimeDeserializer extends JsonDeserializer<ZonedDateTime> {
        @Override
        public ZonedDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
            return dateTimeFormat.parse(jsonParser.getValueAsString());
        }
    }

    @Override
    public ObjectMapper copy() {
        return new BasicJsonObjectMapper();
    }
}

