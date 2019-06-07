package com.extremum.common.mapper;

import com.extremum.common.deserializers.*;
import com.extremum.common.response.Pagination;
import com.extremum.common.serializers.*;
import com.extremum.common.stucts.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;


/**
 * Public object mapper for clients.
 */
public class JsonObjectMapper extends BasicJsonObjectMapper {

    public static JsonObjectMapper createWithoutDescriptorTransfiguration() {
        JsonObjectMapper mapper = new JsonObjectMapper();
        mapper.configure();
        return mapper;
    }

    public static JsonObjectMapper createWithCollectionDescriptors(MapperDependencies dependencies) {
        JsonObjectMapper mapper = new JsonObjectMapper();
        mapper.configure();

        mapper.registerModule(new DescriptorsModule());
        mapper.registerModule(new CollectionDescriptorsModule(dependencies));

        return mapper;
    }

    @Override
    public ObjectMapper copy() {
        return JsonObjectMapper.createWithoutDescriptorTransfiguration();
    }

    /**
     * Masking descriptor details as if it were a plain string
     */
    @Override
    protected SimpleModule createCustomModule() {
        SimpleModule module = super.createCustomModule();

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
        module.addDeserializer(IntegerOrString.class, new IntegerOrStringDeserializer());

        module.addSerializer(IdOrObjectStruct.class, new IdOrObjectStructSerializer(this));

        module.addDeserializer(Pagination.class, new PaginationDeserializer(this));

        return module;
    }

}