package io.extremum.mongo.springdata.reactiverepository;

import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.repository.query.QueryLookupStrategy;

import java.util.Optional;

/**
 * @author rpuch
 */
class ReactiveLookupStrategies {
    private final ReactiveMongoOperations mongoOperations;

    ReactiveLookupStrategies(ReactiveMongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    Optional<QueryLookupStrategy> softDeleteQueryLookupStrategy(Optional<QueryLookupStrategy> optStrategy) {
        return optStrategy.map(this::createSoftDeleteQueryLookupStrategy);
    }

    private QueryLookupStrategy createSoftDeleteQueryLookupStrategy(QueryLookupStrategy strategy) {
        return new SoftDeleteReactiveMongoQueryLookupStrategy(strategy, mongoOperations);
    }
}
