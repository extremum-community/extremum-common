package com.extremum.common.collection.conversion;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.uuid.StandardUUIDGenerator;
import com.extremum.common.uuid.UUIDGenerator;
import com.extremum.common.utils.ReflectionUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author rpuch
 */
public class InMemoryCollectionDescriptorService implements CollectionDescriptorService {
    private final Map<String, CollectionDescriptor> externalIdsToDescriptors = new ConcurrentHashMap<>();
    private final Map<String, CollectionDescriptor> coordinatesToDescriptors = new ConcurrentHashMap<>();

    private final UUIDGenerator uuidGenerator = new StandardUUIDGenerator();

    @Override
    public Optional<CollectionDescriptor> retrieveByExternalId(String externalId) {
        return Optional.ofNullable(externalIdsToDescriptors.get(externalId));
    }

    @Override
    public Optional<CollectionDescriptor> retrieveByCoordinates(String coordinatesString) {
        return Optional.ofNullable(coordinatesToDescriptors.get(coordinatesString));
    }

    @Override
    public void store(CollectionDescriptor descriptor) {
        String externalId = uuidGenerator.generateUUID();
        setCollectionDescriptorId(descriptor, externalId);

        externalIdsToDescriptors.put(descriptor.getExternalId(), descriptor);
        coordinatesToDescriptors.put(descriptor.toCoordinatesString(), descriptor);
    }

    private void setCollectionDescriptorId(CollectionDescriptor descriptor, String externalId) {
        ReflectionUtils.setFieldValue(descriptor, "externalId", externalId);
    }
}
