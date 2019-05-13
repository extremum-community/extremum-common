package com.extremum.common.repository;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.ConvertingParameterAccessor;
import org.springframework.data.mongodb.repository.query.PartTreeMongoQuery;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;

import java.lang.reflect.Method;

/**
 * @author rpuch
 */
public class SoftDeleteMongoQueryLookupStrategy implements QueryLookupStrategy {
    private final QueryLookupStrategy strategy;
    private final MongoOperations mongoOperations;
    private final SoftDeletion softDeletion = new SoftDeletion();

    public SoftDeleteMongoQueryLookupStrategy(QueryLookupStrategy strategy,
            MongoOperations mongoOperations) {
        this.strategy = strategy;
        this.mongoOperations = mongoOperations;
    }

    @Override
    public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
            NamedQueries namedQueries) {
        RepositoryQuery repositoryQuery = strategy.resolveQuery(method, metadata, factory, namedQueries);

        if (method.getAnnotation(SeesSoftlyDeletedRecords.class) != null) {
            return repositoryQuery;
        }

        if (!(repositoryQuery instanceof PartTreeMongoQuery)) {
            return repositoryQuery;
        }
        PartTreeMongoQuery partTreeMongoQuery = (PartTreeMongoQuery) repositoryQuery;

        return new SoftDeletePartTreeMongoQuery(partTreeMongoQuery);
    }

    private class SoftDeletePartTreeMongoQuery extends PartTreeMongoQuery {
        public SoftDeletePartTreeMongoQuery(PartTreeMongoQuery partTreeMongoQuery) {
            super(partTreeMongoQuery.getQueryMethod(), SoftDeleteMongoQueryLookupStrategy.this.mongoOperations);
        }

        @Override
        protected Query createQuery(ConvertingParameterAccessor accessor) {
            Query query = super.createQuery(accessor);
            query.addCriteria(softDeletion.notDeleted());
            return query;
        }
    }
}
