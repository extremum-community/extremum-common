package io.extremum.common.mapper;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.common.descriptor.serde.DescriptorDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * @author rpuch
 */
public class DescriptorsModule extends SimpleModule {
    public DescriptorsModule(MapperDependencies dependencies) {
        addSerializer(Descriptor.class, new ToStringSerializer());
        addDeserializer(Descriptor.class, new DescriptorDeserializer(dependencies.descriptorFactory()));
    }
}
