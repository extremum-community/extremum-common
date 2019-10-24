package io.extremum.common.descriptor.dao.impl;

import io.extremum.common.descriptor.dao.DescriptorDao;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;


@Slf4j
public abstract class BaseDescriptorDao implements DescriptorDao {
    private final DescriptorRepository descriptorRepository;
    private final RMap<String, Descriptor> descriptors;
    private final RMap<String, String> internalIdIndex;
    private final RMap<String, String> collectionCoordinatesToExternalIds;
    private static final int RETRY_ATTEMPTS = 3;

    BaseDescriptorDao(DescriptorRepository descriptorRepository, RMap<String, Descriptor> descriptors,
            RMap<String, String> internalIdIndex, RMap<String, String> collectionCoordinatesToExternalIds) {
        this.descriptorRepository = descriptorRepository;
        this.descriptors = descriptors;
        this.internalIdIndex = internalIdIndex;
        this.collectionCoordinatesToExternalIds = collectionCoordinatesToExternalIds;
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
    public Optional<Descriptor> retrieveByCollectionCoordinates(String collectionCoordinates) {
        String descriptorId = collectionCoordinatesToExternalIds.get(collectionCoordinates);

        return Optional.ofNullable(descriptorId).map(descriptors::get);
    }

    @Override
    public Map<String, String> retrieveMapByExternalIds(Collection<String> externalIds) {
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
        Optional<Descriptor> optionalDesc = descriptorRepository.findByExternalId(descriptor.getExternalId());

        descriptorRepository.save(descriptor);

        if (optionalDesc.isPresent()) {
            try {
                putToMaps(descriptor);
            } catch (RuntimeException e) {
                Descriptor oldDescriptor = optionalDesc.get();
                for (int i = 1; i <= RETRY_ATTEMPTS; i++) {
                    try {
                        descriptorRepository.save(oldDescriptor);
                        break;
                    } catch (Exception ex) {
                        if (i == 3) {
                            log.error("Failed reset to old state descriptor with external id: {}", oldDescriptor.getExternalId());
                        }
                    }
                }
            }
        } else {
            putToMaps(descriptor);
        }
        return descriptor;
    }

    private void putToMaps(Descriptor descriptor) {
        descriptors.put(descriptor.getExternalId(), descriptor);
        if (descriptor.isSingle()) {
            internalIdIndex.put(descriptor.getInternalId(), descriptor.getExternalId());
        }
        if (descriptor.isCollection()) {
            collectionCoordinatesToExternalIds.put(
                    descriptor.getCollection().toCoordinatesString(), descriptor.getExternalId());
        }
    }
}
