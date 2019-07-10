package com.extremum.common.mapper;

import com.extremum.common.deserializers.*;
import com.extremum.common.response.Pagination;
import com.extremum.common.serializers.*;
import com.extremum.common.stucts.DurationVariativeValue;
import com.extremum.common.stucts.IdListOrObjectList;
import com.extremum.common.stucts.IntegerRangeOrValue;
import com.extremum.sharedmodels.basic.IdOrObject;
import com.extremum.sharedmodels.basic.IntegerOrString;
import com.extremum.sharedmodels.basic.StringOrMultilingual;
import com.extremum.sharedmodels.content.Display;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author rpuch
 */
public class BasicSerializationDeserializationModule extends SimpleModule {
    public BasicSerializationDeserializationModule(ObjectMapper mapper) {
        setDeserializerModifier(new EnumDeserializerModifier());
        addSerializer(Enum.class, new EnumSerializer());
        addDeserializer(ObjectId.class, new ObjectIdDeserializer());

        addSerializer(StringOrMultilingual.class, new StringOrMultilingualSerializer());
        addDeserializer(StringOrMultilingual.class, new StringOrMultilingualDeserializer());

        addSerializer(IdListOrObjectList.class, new IdListOrObjectListSerializer());

        addDeserializer(IntegerRangeOrValue.class, new IntegerRangeOrValueDeserializer());
        addSerializer(IntegerRangeOrValue.class, new IntegerRangeOrValueSerializer());

        addDeserializer(DurationVariativeValue.class, new DurationVariativeValueDeserializer());
        addSerializer(DurationVariativeValue.class, new DurationVariativeValueSerializer());

        addDeserializer(Display.class, new DisplayDeserializer(mapper));
        addSerializer(Display.class, new DisplaySerializer());

        addSerializer(IntegerOrString.class, new IntegerOrStringSerializer());
        addDeserializer(IntegerOrString.class, new IntegerOrStringDeserializer());

        addSerializer(IdOrObject.class, new IdOrObjectSerializer(mapper));

        addDeserializer(Pagination.class, new PaginationDeserializer(mapper));
    }

    private static class ObjectIdDeserializer extends StdDeserializer<ObjectId> {
        ObjectIdDeserializer() {
            super(ObjectId.class);
        }

        @Override
        public ObjectId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new ObjectId(p.getValueAsString());
        }
    }

    private static class EnumSerializer extends StdSerializer<Enum> {
        EnumSerializer() {
            super(Enum.class);
        }

        @Override
        public void serialize(Enum value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException {
            actuallySerialize(value, jsonGenerator);
        }

        private void actuallySerialize(Enum value, JsonGenerator jsonGenerator) throws IOException {
            if (value != null) {
                jsonGenerator.writeString(value.name().toLowerCase());
            }
        }

        @Override
        public void serializeWithType(Enum value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider,
                TypeSerializer typeSer) throws IOException {
            actuallySerialize(value, jsonGenerator);
        }
    }

    private static class EnumDeserializerModifier extends BeanDeserializerModifier {
        private static final Logger LOGGER = LoggerFactory.getLogger(EnumDeserializerModifier.class);

        @Override
        public JsonDeserializer<Enum> modifyEnumDeserializer(
                DeserializationConfig deserializationConfig, JavaType javaType, BeanDescription beanDescription, JsonDeserializer<?> jsonDeserializer) {
            return new JsonDeserializer<Enum>() {
                @Override
                public Enum deserialize(
                        JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
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
}
