package com.extremum.common.descriptor.dao.impl;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.dao.DescriptorDao;
import lombok.extern.slf4j.Slf4j;
import org.mongodb.morphia.Datastore;
import org.redisson.api.RMap;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;


@Slf4j
public abstract class BaseDescriptorDao implements DescriptorDao {
    private final Datastore mongoDatastore;
    private final RMap<String, Descriptor> descriptors;
    private final RMap<String, String> internalIdIndex;

    BaseDescriptorDao(Datastore mongoDatastore, RMap<String, Descriptor> descriptors, RMap<String, String> internalIdIndex) {
        this.mongoDatastore = mongoDatastore;
        this.descriptors = descriptors;
        this.internalIdIndex = internalIdIndex;
    }

    @Override
    public Optional<Descriptor> retrieveByExternalId(String externalId) {
        return Optional.ofNullable(descriptors.get(externalId));
    }

    @Override
    public Optional<Descriptor> retrieveByInternalId(String internalId) {
        String descriptorId = internalIdIndex.get(internalId);

        return Optional.ofNullable(descriptorId).map(descriptors::get);
    }

    @Override
    public Map<String, String> retrieveMapByExternalIds(@NotNull Collection<String> externalIds) {
        Map<String, Descriptor> all = descriptors.getAll(new HashSet<>(externalIds));

        return all.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, e -> e.getValue().getInternalId()));
    }

    @Override
    public Map<String, String> retrieveMapByInternalIds(Collection<String> internalIds) {
        return internalIdIndex.getAll(new HashSet<>(internalIds));
    }

    @Override
    public Descriptor store(Descriptor descriptor) {
        Optional<Descriptor> optionalDesc = Optional.ofNullable(mongoDatastore
                .find(Descriptor.class)
                .field("externalId")
                .equal(descriptor.getExternalId())
                .get());

        if (optionalDesc.isPresent()) {
            mongoDatastore.save(descriptor);
            try {
                descriptors.put(descriptor.getExternalId(), descriptor);
                internalIdIndex.put(descriptor.getInternalId(), descriptor.getExternalId());
            } catch (Exception e) {
                Descriptor oldDescriptor = optionalDesc.get();
                for (int i = 1; i <= 3; i++) {
                    try {
                        mongoDatastore.save(oldDescriptor);
                        break;
                    } catch (Exception ex) {
                        if (i == 3) {
                            log.error("Failed reset to old state descriptor with external id: {}", oldDescriptor.getExternalId());
                        }
                    }
                }
            }
        } else {
            mongoDatastore.save(descriptor);
            descriptors.put(descriptor.getExternalId(), descriptor);
            internalIdIndex.put(descriptor.getInternalId(), descriptor.getExternalId());
        }
        return descriptor;
    }
}
