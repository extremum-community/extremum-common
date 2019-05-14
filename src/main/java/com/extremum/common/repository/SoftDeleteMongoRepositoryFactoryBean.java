package com.extremum.common.repository;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

/**
 * Factory bean for {@link MongoRepositoryFactory} extension that makes all automagical
 * queries generated from query methods like <code>Person findByEmail(String email)</code>
 * respect the 'deleted' flag (unless annotated with @{@link SeesSoftlyDeletedRecords)}.
 *
 * It is to be referenced in @{@link org.springframework.data.mongodb.repository.config.EnableMongoRepositories
 * like this:
 *
 * <pre>
 * @EnableMongoRepositories(repositoryBaseClass = BaseMongoRepository.class,
        repositoryFactoryBeanClass = SoftDeleteMongoRepositoryFactoryBean.class,
        basePackages = "com.cybernation.testservice.repositories")
 * </pre>
 *
 * @author rpuch
 */
public class SoftDeleteMongoRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
        extends MongoRepositoryFactoryBean<T, S, ID> {

    public SoftDeleteMongoRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    protected RepositoryFactorySupport getFactoryInstance(MongoOperations operations) {
        return new SoftDeleteMongoRepositoryFactory(operations);
    }
}