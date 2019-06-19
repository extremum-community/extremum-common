package com.extremum.elasticsearch.repositories;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchRepositoryFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;

import java.util.Optional;

/**
 * @author rpuch
 */
public class ExtremumElasticsearchRepositoryFactory extends ElasticsearchRepositoryFactory {
    private final LookupStrategies lookupStrategies;

    public ExtremumElasticsearchRepositoryFactory(ElasticsearchOperations elasticsearchOperations) {
        super(elasticsearchOperations);
        lookupStrategies = new LookupStrategies(elasticsearchOperations);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return SoftDeleteElasticsearchRepository.class;
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(QueryLookupStrategy.Key key,
            QueryMethodEvaluationContextProvider evaluationContextProvider) {
        Optional<QueryLookupStrategy> optStrategy = super.getQueryLookupStrategy(key, evaluationContextProvider);
        return lookupStrategies.softDeleteQueryLookupStrategy(optStrategy);
    }
}
