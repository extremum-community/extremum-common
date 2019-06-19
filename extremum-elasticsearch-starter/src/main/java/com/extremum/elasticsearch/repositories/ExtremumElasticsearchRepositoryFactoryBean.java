package com.extremum.elasticsearch.repositories;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

/**
 * @author rpuch
 */
public class ExtremumElasticsearchRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
        extends ElasticsearchRepositoryFactoryBean<T, S, ID> {
    private ElasticsearchOperations operations;

    public ExtremumElasticsearchRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    public void setElasticsearchOperations(ElasticsearchOperations operations) {
        super.setElasticsearchOperations(operations);
        this.operations = operations;
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory() {
        return new ExtremumElasticsearchRepositoryFactory(operations);
    }
}