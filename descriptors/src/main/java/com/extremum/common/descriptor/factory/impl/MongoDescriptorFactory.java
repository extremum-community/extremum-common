package com.extremum.common.descriptor.factory.impl;

import com.extremum.common.descriptor.factory.DescriptorFactory;
import com.extremum.common.descriptor.Descriptor;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.stream.Collectors;


public final class MongoDescriptorFactory extends DescriptorFactory {

    private static Descriptor.StorageType storageType = Descriptor.StorageType.MONGO;

    private MongoDescriptorFactory() {}

    public static Descriptor create(ObjectId id, String modelType) {
        return DescriptorFactory.create(id.toString(), modelType, storageType);
    }

    public static Descriptor fromInternalId(ObjectId internalId) {
        return fromInternalId(internalId.toString());
    }

    public static Descriptor fromInternalId(String internalId) {
        return DescriptorFactory.fromInternalId(internalId, storageType);
    }

    public static Descriptor fromInternalIdOrNull(String internalId) {
        return DescriptorFactory.fromInternalIdOrNull(internalId, storageType);
    }

    public static List<Descriptor> fromInternalIdListOrNull(List<String> internalIdList) {
        if (internalIdList == null) {
            return null;
        }
        return internalIdList.stream()
                .map(MongoDescriptorFactory::fromInternalIdOrNull)
                .collect(Collectors.toList());
    }

    public static ObjectId resolve(Descriptor descriptor) {
        String internalId = DescriptorFactory.resolve(descriptor, storageType);
        return new ObjectId(internalId);
    }
}
