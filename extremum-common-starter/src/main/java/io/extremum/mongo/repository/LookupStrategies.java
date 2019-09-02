package io.extremum.mongo.repository;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.repository.query.QueryLookupStrategy;

import java.util.Optional;

/**
 * @author rpuch
 */
class LookupStrategies {
    private final MongoOperations mongoOperations;

    LookupStrategies(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    Optional<QueryLookupStrategy> softDeleteQueryLookupStrategy(Optional<QueryLookupStrategy> optStrategy) {
        return optStrategy.map(this::createSoftDeleteQueryLookupStrategy);
    }

    private SoftDeleteMongoQueryLookupStrategy createSoftDeleteQueryLookupStrategy(QueryLookupStrategy strategy) {
        return new SoftDeleteMongoQueryLookupStrategy(strategy, mongoOperations);
    }
}
