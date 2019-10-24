package io.extremum.common.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.mapper.BasicJsonObjectMapper;


/**
 * Public object mapper for clients.
 */
public class SystemJsonObjectMapper extends BasicJsonObjectMapper {
    private final MapperDependencies dependencies;

    public SystemJsonObjectMapper(MapperDependencies dependencies) {
        this.dependencies = dependencies;

        registerModule(new DescriptorsModule(dependencies));
    }

    @Override
    public ObjectMapper copy() {
        return new SystemJsonObjectMapper(dependencies);
    }

}