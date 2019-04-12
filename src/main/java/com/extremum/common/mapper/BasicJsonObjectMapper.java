package com.extremum.common.mapper;

import com.extremum.common.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ofPattern;


/**
 * Created by vov4a on 08.10.15.
 */
public class BasicJsonObjectMapper extends ObjectMapper {

    private static final DateTimeFormatter FORMATTER = ofPattern(DateUtils.FORMAT);

    BasicJsonObjectMapper() {
        super();
        // deserialization
        this.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true);
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

        SimpleModule module = createSimpozioModule();
        this.registerModule(module);

        JavaTimeModule javaTimeModule = createJavaTimeModule();
        this.registerModule(javaTimeModule);

        this.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        this.setDateFormat(DateUtils.DATE_FORMAT);
    }

    protected SimpleModule createSimpozioModule() {
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new EnumDeserializerModifier());
        module.addSerializer(Enum.class, new EnumSerializer());
        return module;
    }

    protected JavaTimeModule createJavaTimeModule() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(ZonedDateTime.class, new ZoneDateTimeSerializer());
        javaTimeModule.addDeserializer(ZonedDateTime.class, new ZoneDateTimeDeserializer());
        return javaTimeModule;
    }

    private static class EnumSerializer extends StdSerializer<Enum> {
        EnumSerializer() {
            super(Enum.class);
        }

        @Override
        public void serialize(Enum value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (value != null) {
                jsonGenerator.writeString(value.name().toLowerCase());
            }
        }
    }

    private static class EnumDeserializerModifier extends BeanDeserializerModifier {
        private static final Logger LOGGER = LoggerFactory.getLogger(EnumDeserializerModifier.class);

        @Override
        public JsonDeserializer<Enum> modifyEnumDeserializer(DeserializationConfig deserializationConfig, JavaType javaType, BeanDescription beanDescription, JsonDeserializer<?> jsonDeserializer) {
            return new JsonDeserializer<Enum>() {
                @Override
                public Enum deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                    Class<? extends Enum> rawClass = (Class<Enum<?>>) javaType.getRawClass();
                    for (Method method : rawClass.getMethods()) {
                        if (method.getName().equals("fromString") && method.getParameterCount() == 1) {
                            try {
                                return (Enum) method.invoke(rawClass, jsonParser.getValueAsString());
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                LOGGER.error("Can't retrieve enum " + rawClass.getName() +
                                        " from string value " + jsonParser.getValueAsString(), e);
                            }
                        }
                    }

                    return Enum.valueOf(rawClass, jsonParser.getValueAsString().toUpperCase());
                }
            };
        }
    }

    private class ZoneDateTimeSerializer extends JsonSerializer<ZonedDateTime> {
        @Override
        public void serialize(ZonedDateTime zonedDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(zonedDateTime.format(FORMATTER));
        }
    }

    private class ZoneDateTimeDeserializer extends JsonDeserializer<ZonedDateTime> {
        @Override
        public ZonedDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return ZonedDateTime.parse(jsonParser.getValueAsString(), FORMATTER);
        }
    }
}

