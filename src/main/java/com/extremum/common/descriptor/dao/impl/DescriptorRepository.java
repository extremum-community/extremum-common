package com.extremum.common.descriptor.dao.impl;

import com.extremum.common.descriptor.Descriptor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author rpuch
 */
@Repository
public interface DescriptorRepository extends MongoRepository<Descriptor, String> {
    Optional<Descriptor> findByExternalId(String externalId);

    Optional<Descriptor> findByInternalId(String internalId);

    @Query(value="{}", fields="{_id : 1}")
    List<Descriptor> findAllExternalIds();

    @Query(value="{}", fields="{_id : 0, internalId : 1}")
    List<Descriptor> findAllInternalIds();
}
