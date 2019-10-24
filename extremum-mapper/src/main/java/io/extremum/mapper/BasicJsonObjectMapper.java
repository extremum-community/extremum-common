package io.extremum.mapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.extremum.mapper.module.BasicSerializationDeserializationModule;
import io.extremum.mapper.module.StringOrObjectModule;
import io.extremum.sharedmodels.constants.DateConstants;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static java.time.format.DateTimeFormatter.ofPattern;


/**
 * Created by vov4a on 08.10.15.
 */
public class BasicJsonObjectMapper extends ObjectMapper {

    private static final DateTimeFormatter FORMATTER = ofPattern(DateConstants.FORMAT);

    public BasicJsonObjectMapper() {
        // deserialization
        this.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true);
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

        this.registerModule(new BasicSerializationDeserializationModule(this));
        this.registerModule(new StringOrObjectModule());
        this.registerModule(createJavaTimeModule());

        this.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        this.setDateFormat(new SimpleDateFormat(DateConstants.FORMAT, Locale.US));
    }

    private JavaTimeModule createJavaTimeModule() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(ZonedDateTime.class, new ZoneDateTimeSerializer());
        javaTimeModule.addDeserializer(ZonedDateTime.class, new ZoneDateTimeDeserializer());
        return javaTimeModule;
    }

    private static class ZoneDateTimeSerializer extends JsonSerializer<ZonedDateTime> {
        @Override
        public void serialize(ZonedDateTime zonedDateTime, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(zonedDateTime.format(FORMATTER));
        }
    }

    private static class ZoneDateTimeDeserializer extends JsonDeserializer<ZonedDateTime> {
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

