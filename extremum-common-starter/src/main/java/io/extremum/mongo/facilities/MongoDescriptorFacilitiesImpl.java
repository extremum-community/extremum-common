package io.extremum.mongo.facilities;

import io.extremum.common.descriptor.dao.DescriptorDao;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.DescriptorResolver;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.DescriptorNotFoundException;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class MongoDescriptorFacilitiesImpl implements MongoDescriptorFacilities {
    private static final Descriptor.StorageType STORAGE_TYPE = Descriptor.StorageType.MONGO;

    private final DescriptorFactory descriptorFactory;
    private final DescriptorSaver descriptorSaver;
    private final DescriptorDao descriptorDao;

    private final DescriptorReadinessValidation descriptorReadinessValidation = new DescriptorReadinessValidation();

    @Override
    public Descriptor create(ObjectId id, String modelType) {
        return descriptorSaver.createAndSave(id.toString(), modelType, STORAGE_TYPE);
    }

    @Override
    public Descriptor fromInternalId(ObjectId internalId) {
        return fromInternalId(internalId.toString());
    }

    @Override
    public Descriptor fromInternalId(String internalId) {
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

    @Override
    public Descriptor makeDescriptorReady(String descriptorExternalId, String modelType) {
        Descriptor descriptor = descriptorDao.retrieveByExternalId(descriptorExternalId)
                .orElseThrow(() -> doesNotExistException(descriptorExternalId));

        descriptorReadinessValidation.validateDescriptorIsNotReady(descriptorExternalId, descriptor);

        descriptor.setReadiness(Descriptor.Readiness.READY);
        descriptor.setModelType(modelType);

        return descriptorDao.store(descriptor);
    }

    private DescriptorNotFoundException doesNotExistException(String descriptorExternalId) {
        String errorMessage = String.format("No descriptor with externalId '%s' exists", descriptorExternalId);
        return new DescriptorNotFoundException(errorMessage);
    }
}
