package com.extremum.common.collection.conversion;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.service.CollectionDescriptorService;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author rpuch
 */
public class InMemoryCollectionDescriptorService implements CollectionDescriptorService {
    private final Map<String, CollectionDescriptor> externalIdsToDescriptors = new ConcurrentHashMap<>();
    private final Map<String, CollectionDescriptor> coordinatesToDescriptors = new ConcurrentHashMap<>();

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
        String externalId = UUID.randomUUID().toString();
        setCollectionDescriptorId(descriptor, externalId);

        externalIdsToDescriptors.put(descriptor.getExternalId(), descriptor);
        coordinatesToDescriptors.put(descriptor.toCoordinatesString(), descriptor);
    }

    private void setCollectionDescriptorId(CollectionDescriptor descriptor,
            String externalId) {
        Field field = findIdField();
        field.setAccessible(true);
        setIdValue(descriptor, field, externalId);
    }

    private void setIdValue(CollectionDescriptor descriptor, Field field, String externalId) {
        try {
            field.set(descriptor, externalId);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot set ID value", e);
        }
    }

    private Field findIdField() {
        Field field;
        try {
            field = CollectionDescriptor.class.getDeclaredField("externalId");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("No such field, probaly a typo", e);
        }
        return field;
    }
}
