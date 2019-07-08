package com.extremum.common.descriptor.factory.impl;

import com.extremum.common.descriptor.factory.MongoDescriptorFacilities;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.DescriptorFactory;
import com.extremum.common.descriptor.factory.DescriptorResolver;
import com.extremum.common.descriptor.factory.DescriptorSaver;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class MongoDescriptorFacilitiesImpl implements MongoDescriptorFacilities {
    private static final Descriptor.StorageType STORAGE_TYPE = Descriptor.StorageType.MONGO;

    private final DescriptorFactory descriptorFactory;
    private final DescriptorSaver descriptorSaver;
    private final DescriptorService descriptorService;

    @Override
    public Descriptor create(ObjectId id, String modelType) {
        return descriptorSaver.createAndSave(id.toString(), modelType, STORAGE_TYPE);
    }

    @Override
    public Descriptor createWithNewInternalId(String modelType) {
        ObjectId objectId = new ObjectId();
        Descriptor descriptor = Descriptor.builder()
                .externalId(descriptorService.createExternalId())
                .internalId(objectId.toString())
                .modelType(modelType)
                .storageType(STORAGE_TYPE)
                .build();
        return descriptorService.store(descriptor);
    }

    @Override
    public Descriptor fromInternalId(ObjectId internalId) {
        return fromInternalId(internalId.toString());
    }

    private Descriptor fromInternalId(String internalId) {
        return descriptorFactory.fromInternalId(internalId, STORAGE_TYPE);
    }

    @Override
    public List<String> getInternalIdList(List<Descriptor> descriptors) {
        return descriptors.stream()
                .map(Descriptor::getInternalId)
                .collect(Collectors.toList());
    }

    private Descriptor fromInternalIdOrNull(String internalId) {
        return descriptorFactory.fromInternalIdOrNull(internalId, STORAGE_TYPE);
    }

    @Override
    public List<Descriptor> fromInternalIdListOrNull(List<String> internalIdList) {
        if (internalIdList == null) {
            return null;
        }
        return internalIdList.stream()
                .map(this::fromInternalIdOrNull)
                .collect(Collectors.toList());
    }

    @Override
    public ObjectId resolve(Descriptor descriptor) {
        String internalId = DescriptorResolver.resolve(descriptor, STORAGE_TYPE);
        return new ObjectId(internalId);
    }
}
