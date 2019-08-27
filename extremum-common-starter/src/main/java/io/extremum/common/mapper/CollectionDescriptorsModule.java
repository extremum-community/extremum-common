package io.extremum.common.mapper;

import io.extremum.common.collection.serde.CollectionDescriptorDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;

/**
 * @author rpuch
 */
public class CollectionDescriptorsModule extends SimpleModule {
    public CollectionDescriptorsModule(MapperDependencies collectionDescriptorTransfigurationDependencies) {
        addSerializer(CollectionDescriptor.class, new ToStringSerializer());
        addDeserializer(CollectionDescriptor.class, new CollectionDescriptorDeserializer(
                collectionDescriptorTransfigurationDependencies.collectionDescriptorService()));
    }
}
