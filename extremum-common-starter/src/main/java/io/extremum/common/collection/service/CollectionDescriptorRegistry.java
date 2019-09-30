package io.extremum.common.collection.service;

import io.extremum.sharedmodels.descriptor.Descriptor;

public interface CollectionDescriptorRegistry {
    Descriptor freeCollection(String name);
}
