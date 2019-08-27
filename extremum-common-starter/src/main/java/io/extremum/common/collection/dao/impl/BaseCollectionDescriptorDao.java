package io.extremum.common.collection.dao.impl;

import io.extremum.common.collection.dao.CollectionDescriptorDao;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;

import java.util.Optional;


@Slf4j
public abstract class BaseCollectionDescriptorDao implements CollectionDescriptorDao {
    private final CollectionDescriptorRepository repository;
    private final RMap<String, CollectionDescriptor> descriptors;
    private final RMap<String, String> coordinatesToExternalIds;
    private static final int RETRY_ATTEMPTS = 3;

    BaseCollectionDescriptorDao(CollectionDescriptorRepository repository,
            RMap<String, CollectionDescriptor> descriptors,
            RMap<String, String> coordinatesToExternalIds) {
        this.repository = repository;
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
        Optional<CollectionDescriptor> optionalDesc = repository.findByExternalId(descriptor.getExternalId());

        repository.save(descriptor);

        if (optionalDesc.isPresent()) {
            try {
                descriptors.put(descriptor.getExternalId(), descriptor);
                coordinatesToExternalIds.put(descriptor.toCoordinatesString(), descriptor.getExternalId());
            } catch (RuntimeException e) {
                CollectionDescriptor oldDescriptor = optionalDesc.get();
                for (int i = 1; i <= RETRY_ATTEMPTS; i++) {
                    try {
                        repository.save(oldDescriptor);
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
