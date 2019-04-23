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
    private static final int RETRY_ATTEMPTS = 3;

    BaseCollectionDescriptorDao(Datastore mongoDatastore, RMap<String, CollectionDescriptor> descriptors) {
        this.mongoDatastore = mongoDatastore;
        this.descriptors = descriptors;
    }

    @Override
    public Optional<CollectionDescriptor> retrieveByExternalId(String externalId) {
        return Optional.ofNullable(descriptors.get(externalId));
    }

    @Override
    public CollectionDescriptor store(CollectionDescriptor descriptor) {
        Optional<CollectionDescriptor> optionalDesc = Optional.ofNullable(mongoDatastore
                .find(CollectionDescriptor.class)
                .field("externalId")
                .equal(descriptor.getExternalId())
                .get());

        mongoDatastore.save(descriptor);

        if (optionalDesc.isPresent()) {
            try {
                descriptors.put(descriptor.getExternalId(), descriptor);
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
        }

        return descriptor;
    }
}
