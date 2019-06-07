package com.extremum.common.mapper;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.serde.DescriptorDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * @author rpuch
 */
public class DescriptorsModule extends SimpleModule {
    public DescriptorsModule() {
        addSerializer(Descriptor.class, new ToStringSerializer());
        addDeserializer(Descriptor.class, new DescriptorDeserializer());
    }
}
