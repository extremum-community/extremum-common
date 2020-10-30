package io.extremum.mapper.jackson.module;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.extremum.mapper.jackson.deserializer.DisplayDeserializer;
import io.extremum.mapper.jackson.deserializer.DurationVariativeValueDeserializer;
import io.extremum.mapper.jackson.deserializer.IntegerOrStringDeserializer;
import io.extremum.mapper.jackson.deserializer.IntegerRangeOrValueDeserializer;
import io.extremum.mapper.jackson.deserializer.PaginationDeserializer;
import io.extremum.mapper.jackson.deserializer.StringOrMultilingualDeserializer;
import io.extremum.mapper.jackson.serializer.DisplaySerializer;
import io.extremum.mapper.jackson.serializer.DurationVariativeValueSerializer;
import io.extremum.mapper.jackson.serializer.IdListOrObjectListSerializer;
import io.extremum.mapper.jackson.serializer.IdOrObjectSerializer;
import io.extremum.mapper.jackson.serializer.IntegerOrStringSerializer;
import io.extremum.mapper.jackson.serializer.IntegerRangeOrValueSerializer;
import io.extremum.mapper.jackson.serializer.StringOrMultilingualSerializer;
import io.extremum.sharedmodels.basic.IdOrObject;
import io.extremum.sharedmodels.basic.IntegerOrString;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import io.extremum.sharedmodels.content.Display;
import io.extremum.sharedmodels.dto.Pagination;
import io.extremum.sharedmodels.structs.DurationVariativeValue;
import io.extremum.sharedmodels.structs.IdListOrObjectList;
import io.extremum.sharedmodels.structs.IntegerRangeOrValue;
import org.bson.types.ObjectId;

import java.io.IOException;

/**
 * @author rpuch
 */
public class BasicSerializationDeserializationModule extends SimpleModule {
    public BasicSerializationDeserializationModule(ObjectMapper mapper) {
        addSerializer(ObjectId.class, new ToStringSerializer());
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

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);

        addLowercasingEnumIntrospectorWithPriorityLowerThanStandard(context);
    }

    private void addLowercasingEnumIntrospectorWithPriorityLowerThanStandard(SetupContext context) {
        context.appendAnnotationIntrospector(new LowercasingEnumAnnotationIntrospector());
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

    private static class LowercasingEnumAnnotationIntrospector extends NopAnnotationIntrospector {
        @Override
        public String[] findEnumValues(Class<?> enumType, Enum<?>[] enumValues, String[] names) {
            for (int i = 0; i < enumValues.length; i++) {
                if (names[i] == null) {
                    names[i] = enumValues[i].name().toLowerCase();
                }
            }
            return names;
        }
    }
}
