package io.extremum.common.collection.dao.impl;

import io.extremum.common.collection.CollectionDescriptor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author rpuch
 */
@Repository
public interface CollectionDescriptorRepository extends MongoRepository<CollectionDescriptor, String> {
    Optional<CollectionDescriptor> findByExternalId(String externalId);

    Optional<CollectionDescriptor> findByCoordinatesString(String internalId);

    @Query(value="{}", fields="{_id : 1}")
    List<CollectionDescriptor> findAllExternalIds();

    @Query(value="{}", fields="{_id : 0, coordinatesString: 1}")
    List<CollectionDescriptor> findAllCoordinatesStrings();
}
