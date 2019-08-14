package io.extremum.common.repository.mongo;

import io.extremum.common.models.annotation.HardDelete;
import io.extremum.common.repository.SeesSoftlyDeletedRecords;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;

import java.util.Optional;

/**
 * {@link MongoRepositoryFactory} extension that chooses automatically
 * whether to use the usual 'hard-delete' logic or 'soft-delete' logic.
 * For 'soft-delete' flavor, the repository makes all automagical
 * queries generated from query methods like <code>Person findByEmail(String email)</code>
 * respect the 'deleted' flag (unless annotated with &#064;{@link SeesSoftlyDeletedRecords}).
 *
 * @author rpuch
 * @see SeesSoftlyDeletedRecords
 */
public class ExtremumMongoRepositoryFactory extends MongoRepositoryFactory {
    private final Class<?> repositoryInterface;
    private final LookupStrategies lookupStrategies;

    public ExtremumMongoRepositoryFactory(Class<?> repositoryInterface, MongoOperations mongoOperations) {
        super(mongoOperations);
        this.repositoryInterface = repositoryInterface;
        lookupStrategies = new LookupStrategies(mongoOperations);
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

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        if (isSoftDelete()) {
            return SoftDeleteMongoRepository.class;
        } else {
            return HardDeleteMongoRepository.class;
        }
    }

    private boolean isSoftDelete() {
        RepositoryMetadata metadata = getRepositoryMetadata(repositoryInterface);
        boolean isHardDelete = metadata.getDomainType().isAnnotationPresent(HardDelete.class);
        return !isHardDelete;
    }
}