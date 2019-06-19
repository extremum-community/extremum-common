package com.extremum.elasticsearch.repositories;

import com.extremum.common.models.annotation.HardDelete;
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
    private final Class<?> repositoryInterface;
    private final LookupStrategies lookupStrategies;

    public ExtremumElasticsearchRepositoryFactory(Class<?> repositoryInterface,
            ElasticsearchOperations elasticsearchOperations) {
        super(elasticsearchOperations);
        this.repositoryInterface = repositoryInterface;
        lookupStrategies = new LookupStrategies(elasticsearchOperations);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        if (isSoftDelete()) {
            return SoftDeleteElasticsearchRepository.class;
        } else {
            return HardDeleteElasticsearchRepository.class;
        }
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(QueryLookupStrategy.Key key,
            QueryMethodEvaluationContextProvider evaluationContextProvider) {
        if (isSoftDelete()) {
            Optional<QueryLookupStrategy> optStrategy = super.getQueryLookupStrategy(key, evaluationContextProvider);
            return lookupStrategies.softDeleteQueryLookupStrategy(optStrategy);
        } else {
            return super.getQueryLookupStrategy(key, evaluationContextProvider);
        }
    }

    private boolean isSoftDelete() {
        RepositoryMetadata metadata = getRepositoryMetadata(repositoryInterface);
        boolean isHardDelete = metadata.getDomainType().isAnnotationPresent(HardDelete.class);
        return !isHardDelete;
    }
}
