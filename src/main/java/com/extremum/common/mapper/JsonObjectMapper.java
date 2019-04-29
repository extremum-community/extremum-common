package com.extremum.common.mapper;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.serde.CollectionDescriptorDeserializer;
import com.extremum.common.descriptor.serde.DescriptorDeserializer;
import com.extremum.common.deserializers.DisplayDeserializer;
import com.extremum.common.deserializers.DurationVariativeValueDeserializer;
import com.extremum.common.deserializers.IntegerRangeOrValueDeserializer;
import com.extremum.common.deserializers.MultilingualObjectDeserializer;
import com.extremum.common.serializers.DisplaySerializer;
import com.extremum.common.serializers.DurationVariativeValueSerializer;
import com.extremum.common.serializers.IdListOrObjectListStructSerializer;
import com.extremum.common.serializers.IntegerOrStringSerializer;
import com.extremum.common.serializers.IntegerRangeOrValueSerializer;
import com.extremum.common.serializers.MultilingualObjectSerializer;
import com.extremum.common.stucts.Display;
import com.extremum.common.stucts.DurationVariativeValue;
import com.extremum.common.stucts.IdListOrObjectListStruct;
import com.extremum.common.stucts.IntegerOrString;
import com.extremum.common.stucts.IntegerRangeOrValue;
import com.extremum.common.stucts.MultilingualObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;


/**
 * Public object mapper for clients.
 */
public class JsonObjectMapper extends BasicJsonObjectMapper {
    private boolean descriptorTransfigurationDisabled;

    public JsonObjectMapper(boolean disableDescriptorTransfiguration) {
        super();
        descriptorTransfigurationDisabled = disableDescriptorTransfiguration;
    }

    public static JsonObjectMapper createdWithoutDescriptorTransfiguration() {
        JsonObjectMapper mapper = new JsonObjectMapper(true);
        mapper.configure();
        return mapper;
    }

    public static JsonObjectMapper createdMapper() {
        JsonObjectMapper mapper = new JsonObjectMapper(false);
        mapper.configure();
        return mapper;
    }

    @Override
    public ObjectMapper copy() {
        return JsonObjectMapper.createdWithoutDescriptorTransfiguration();
    }

    /**
     * Masking descriptor details as if it were a plain string
     */
    @Override
    protected SimpleModule createCustomModule() {
        SimpleModule module = super.createCustomModule();

        if (!descriptorTransfigurationDisabled) {
            module.addSerializer(Descriptor.class, new ToStringSerializer());
            module.addDeserializer(Descriptor.class, new DescriptorDeserializer());

            module.addSerializer(CollectionDescriptor.class, new ToStringSerializer());
            module.addDeserializer(CollectionDescriptor.class, new CollectionDescriptorDeserializer());
        }

        module.addSerializer(MultilingualObject.class, new MultilingualObjectSerializer());
        module.addDeserializer(MultilingualObject.class, new MultilingualObjectDeserializer());

        module.addSerializer(IdListOrObjectListStruct.class, new IdListOrObjectListStructSerializer());

        module.addDeserializer(IntegerRangeOrValue.class, new IntegerRangeOrValueDeserializer());
        module.addSerializer(IntegerRangeOrValue.class, new IntegerRangeOrValueSerializer());

        module.addDeserializer(DurationVariativeValue.class, new DurationVariativeValueDeserializer());
        module.addSerializer(DurationVariativeValue.class, new DurationVariativeValueSerializer());

        module.addDeserializer(Display.class, new DisplayDeserializer(this));
        module.addSerializer(Display.class, new DisplaySerializer());

        module.addSerializer(IntegerOrString.class, new IntegerOrStringSerializer());

        return module;
    }
}