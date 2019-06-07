package com.extremum.common.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Public object mapper for clients.
 */
public class JsonObjectMapper extends BasicJsonObjectMapper {
    private final MapperDependencies dependencies;

    public JsonObjectMapper(MapperDependencies dependencies) {
        this.dependencies = dependencies;

        registerModule(new DescriptorsModule());
        registerModule(new CollectionDescriptorsModule(dependencies));
    }

    @Override
    public ObjectMapper copy() {
        return new JsonObjectMapper(dependencies);
    }

}