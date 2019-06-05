package com.extremum.common.mapper;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.serde.CollectionDescriptorDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

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
