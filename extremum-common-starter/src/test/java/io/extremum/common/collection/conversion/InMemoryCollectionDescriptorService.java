package io.extremum.common.collection.conversion;

import io.extremum.common.collection.CollectionDescriptor;
import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.common.uuid.StandardUUIDGenerator;
import io.extremum.common.uuid.UUIDGenerator;
import io.extremum.common.utils.ReflectionUtils;

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
