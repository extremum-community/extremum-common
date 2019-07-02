package com.extremum.common.descriptor.factory.impl;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.DescriptorFactory;
import com.extremum.common.descriptor.factory.DescriptorSaver;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class MongoDescriptorFactory {
    private static final Descriptor.StorageType STORAGE_TYPE = Descriptor.StorageType.MONGO;

    private final DescriptorSaver descriptorSaver;

    public Descriptor create(ObjectId id, String modelType) {
        return descriptorSaver.create(id.toString(), modelType, STORAGE_TYPE);
    }

    public static Descriptor fromInternalId(ObjectId internalId) {
        return fromInternalId(internalId.toString());
    }

    public static Descriptor fromInternalId(String internalId) {
        return DescriptorFactory.fromInternalId(internalId, STORAGE_TYPE);
    }

    public List<String> getInternalIdList(List<Descriptor> descriptors) {
        return descriptors.stream()
                .map(Descriptor::getInternalId)
                .collect(Collectors.toList());
    }

    private Descriptor fromInternalIdOrNull(String internalId) {
        return DescriptorFactory.fromInternalIdOrNull(internalId, STORAGE_TYPE);
    }

    public List<Descriptor> fromInternalIdListOrNull(List<String> internalIdList) {
        if (internalIdList == null) {
            return null;
        }
        return internalIdList.stream()
                .map(this::fromInternalIdOrNull)
                .collect(Collectors.toList());
    }

    public static ObjectId resolve(Descriptor descriptor) {
        String internalId = DescriptorFactory.resolve(descriptor, STORAGE_TYPE);
        return new ObjectId(internalId);
    }
}
