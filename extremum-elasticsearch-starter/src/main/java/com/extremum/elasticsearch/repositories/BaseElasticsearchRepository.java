package com.extremum.elasticsearch.repositories;

import com.extremum.common.utils.StreamUtils;
import com.extremum.elasticsearch.dao.ElasticsearchCommonDao;
import com.extremum.elasticsearch.model.ElasticsearchCommonModel;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;
import org.springframework.data.elasticsearch.repository.support.SimpleElasticsearchRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author rpuch
 */
abstract class BaseElasticsearchRepository<T extends ElasticsearchCommonModel>
        extends SimpleElasticsearchRepository<T, String> implements ElasticsearchCommonDao<T> {
    protected BaseElasticsearchRepository(ElasticsearchEntityInformation<T, String> metadata,
            ElasticsearchOperations elasticsearchOperations) {
        super(metadata, elasticsearchOperations);
    }

    @Override
    public List<T> findAll() {
        Iterable<T> all = super.findAll();
        return iterableToList(all);
    }

    private <S> List<S> iterableToList(Iterable<S> iterable) {
        return StreamUtils.fromIterable(iterable).collect(Collectors.toList());
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        Iterable<S> savedEntities = super.saveAll(entities);
        return iterableToList(savedEntities);
    }


    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException("We don't allow to delete all the records in one go");
    }
}
