package com.extremum.common.collection.dao;

import com.extremum.common.collection.CollectionDescriptor;
import lombok.extern.slf4j.Slf4j;
import org.mongodb.morphia.Datastore;
import org.redisson.api.RMap;

import java.util.Optional;


@Slf4j
public abstract class BaseCollectionDescriptorDao implements CollectionDescriptorDao {
    private final Datastore mongoDatastore;
    private final RMap<String, CollectionDescriptor> descriptors;
    private final RMap<String, String> coordinatesToExternalIds;
    private static final int RETRY_ATTEMPTS = 3;

    BaseCollectionDescriptorDao(Datastore mongoDatastore, RMap<String, CollectionDescriptor> descriptors,
            RMap<String, String> coordinatesToExternalIds) {
        this.mongoDatastore = mongoDatastore;
        this.descriptors = descriptors;
        this.coordinatesToExternalIds = coordinatesToExternalIds;
    }

    @Override
    public Optional<CollectionDescriptor> retrieveByExternalId(String externalId) {
        return Optional.ofNullable(descriptors.get(externalId));
    }

    @Override
    public Optional<CollectionDescriptor> retrieveByCoordinates(String coordinatesString) {
        String descriptorId = coordinatesToExternalIds.get(coordinatesString);
        return Optional.ofNullable(descriptorId).map(descriptors::get);
    }

    @Override
    public CollectionDescriptor store(CollectionDescriptor descriptor) {
        Optional<CollectionDescriptor> optionalDesc = Optional.ofNullable(mongoDatastore
                .find(CollectionDescriptor.class)
                .field(CollectionDescriptor.FIELDS.externalId.name())
                .equal(descriptor.getExternalId())
                .get());

        mongoDatastore.save(descriptor);

        if (optionalDesc.isPresent()) {
            try {
                descriptors.put(descriptor.getExternalId(), descriptor);
                coordinatesToExternalIds.put(descriptor.toCoordinatesString(), descriptor.getExternalId());
            } catch (RuntimeException e) {
                CollectionDescriptor oldDescriptor = optionalDesc.get();
                for (int i = 1; i <= RETRY_ATTEMPTS; i++) {
                    try {
                        mongoDatastore.save(oldDescriptor);
                        break;
                    } catch (Exception ex) {
                        if (i == 3) {
                            log.error("Failed reset to old state collection descriptor with external id: {}",
                                    oldDescriptor.getExternalId());
                        }
                    }
                }
            }
        } else {
            descriptors.put(descriptor.getExternalId(), descriptor);
            coordinatesToExternalIds.put(descriptor.toCoordinatesString(), descriptor.getExternalId());
        }

        return descriptor;
    }
}
