package io.extremum.common.descriptor.dao.impl;

import io.extremum.common.descriptor.dao.DescriptorDao;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.redisson.api.RMap;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.*;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.springframework.data.mongodb.core.query.Criteria.where;


public abstract class BaseDescriptorDao implements DescriptorDao {
    private final DescriptorRepository descriptorRepository;
    private final MongoOperations descriptorMongoOperations;
    private final RMap<String, Descriptor> descriptors;
    private final RMap<String, String> internalIdIndex;
    private final RMap<String, String> collectionCoordinatesToExternalIds;

    private final DescriptorInitializations initializations = new DescriptorInitializations();

    BaseDescriptorDao(DescriptorRepository descriptorRepository, MongoOperations descriptorMongoOperations,
            RMap<String, Descriptor> descriptors,
            RMap<String, String> internalIdIndex, RMap<String, String> collectionCoordinatesToExternalIds) {
        this.descriptorRepository = descriptorRepository;
        this.descriptorMongoOperations = descriptorMongoOperations;
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
        initializations.fillCreatedAndModifiedDatesManuallyToHaveFullyFilledObjectInRedis(descriptor);

        boolean initializeVersionManually = initializations.shouldInitializeVersionManually(descriptor);

        initializations.fillVersionIfNeededToHaveFullyFilledObjectInRedis(descriptor, initializeVersionManually);
        putToMaps(descriptor);

        initializations.removeVersionIfItWasSetManually(descriptor, initializeVersionManually);
        return descriptorMongoOperations.save(descriptor);
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

    @Override
    public List<Descriptor> storeBatch(List<Descriptor> descriptorsToSave) {
        descriptorsToSave.forEach(initializations::fillCreatedAndModifiedDatesManuallyToHaveFullyFilledObjectInRedis);

        Boolean[] initializeVersionManually = descriptorsToSave.stream()
                .map(initializations::shouldInitializeVersionManually)
                .toArray(Boolean[]::new);

        for (int i = 0; i < descriptorsToSave.size(); i++) {
            initializations.fillVersionIfNeededToHaveFullyFilledObjectInRedis(descriptorsToSave.get(i),
                    initializeVersionManually[i]);
        }
        putManyToMaps(descriptorsToSave);

        for (int i = 0; i < descriptorsToSave.size(); i++) {
            initializations.removeVersionIfItWasSetManually(descriptorsToSave.get(i), initializeVersionManually[i]);
        }
        return descriptorRepository.saveAll(descriptorsToSave);
    }

    private void putManyToMaps(List<Descriptor> descriptorsToSave) {
        Map<String, Descriptor> mapByExternalId = descriptorsToSave.stream()
                .collect(toMap(Descriptor::getExternalId, identity()));
        descriptors.putAll(mapByExternalId);

        Map<String, String> mapByInternalId = descriptorsToSave.stream()
                .filter(Descriptor::isSingle)
                .collect(toMap(Descriptor::getInternalId, Descriptor::getExternalId));
        internalIdIndex.putAll(mapByInternalId);

        Map<String, String> mapByCollectionCoordinates = descriptorsToSave.stream()
                .filter(Descriptor::isCollection)
                .collect(toMap(descriptor ->
                        descriptor.getCollection().toCoordinatesString(), Descriptor::getExternalId));
        collectionCoordinatesToExternalIds.putAll(mapByCollectionCoordinates);
    }

    @Override
    public void destroyBatch(List<Descriptor> descriptorsToDestroy) {
        String[] externalIds = descriptorsToDestroy.stream()
                .map(Descriptor::getExternalId)
                .toArray(String[]::new);

        destroyInMongo(externalIds);

        descriptors.fastRemove(externalIds);

        String[] internalIds = descriptorsToDestroy.stream()
                .filter(Descriptor::isSingle)
                .map(Descriptor::getInternalId)
                .toArray(String[]::new);
        internalIdIndex.fastRemove(internalIds);

        String[] coordinateStrings = descriptorsToDestroy.stream()
                .filter(Descriptor::isCollection)
                .map(descriptor -> descriptor.getCollection().toCoordinatesString())
                .toArray(String[]::new);
        collectionCoordinatesToExternalIds.fastRemove(coordinateStrings);
    }

    private void destroyInMongo(String[] externalIds) {
        Criteria criteria = where("_id").in(Arrays.asList(externalIds));
        descriptorMongoOperations.remove(new Query(criteria), Descriptor.class);
    }
}
