package com.extremum.common.mapper;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.serde.DescriptorDeserializer;
import com.extremum.common.deserializers.DurationVariativeValueDeserializer;
import com.extremum.common.deserializers.IntegerRangeOrValueDeserializer;
import com.extremum.common.deserializers.MultilingualObjectDeserializer;
import com.extremum.common.serializers.DurationVariativeValueSerializer;
import com.extremum.common.serializers.IdListOrObjectListStructSerializer;
import com.extremum.common.serializers.IntegerRangeOrValueSerializer;
import com.extremum.common.serializers.MultilingualObjectSerializer;
import com.extremum.common.stucts.DurationVariativeValue;
import com.extremum.common.stucts.IdListOrObjectListStruct;
import com.extremum.common.stucts.IntegerRangeOrValue;
import com.extremum.common.stucts.MultilingualObject;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;


/**
 * Public object mapper for clients.
 */
public class JsonObjectMapper extends BasicJsonObjectMapper {
    /**
     * Masking descriptor details as if it were a plain string
     */
    @Override
    protected SimpleModule createSimpozioModule() {
        SimpleModule module = super.createSimpozioModule();

        module.addSerializer(Descriptor.class, new ToStringSerializer());
        module.addDeserializer(Descriptor.class, new DescriptorDeserializer());

        module.addSerializer(MultilingualObject.class, new MultilingualObjectSerializer());
        module.addDeserializer(MultilingualObject.class, new MultilingualObjectDeserializer());

        module.addSerializer(IdListOrObjectListStruct.class, new IdListOrObjectListStructSerializer());

        module.addDeserializer(IntegerRangeOrValue.class, new IntegerRangeOrValueDeserializer());
        module.addSerializer(IntegerRangeOrValue.class, new IntegerRangeOrValueSerializer());

        module.addDeserializer(DurationVariativeValue.class, new DurationVariativeValueDeserializer());
        module.addSerializer(DurationVariativeValue.class, new DurationVariativeValueSerializer());

        return module;
    }
}
