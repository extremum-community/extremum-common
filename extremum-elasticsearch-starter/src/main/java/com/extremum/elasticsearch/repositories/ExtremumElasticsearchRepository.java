package com.extremum.elasticsearch.repositories;

import com.extremum.elasticsearch.model.ElasticsearchCommonModel;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;

import java.util.List;
import java.util.Map;

/**
 * @author rpuch
 */
public class ExtremumElasticsearchRepository<T extends ElasticsearchCommonModel> extends BaseElasticsearchRepository<T> {
    public ExtremumElasticsearchRepository(
            ElasticsearchEntityInformation<T, String> metadata,
            ElasticsearchOperations elasticsearchOperations) {
        super(metadata, elasticsearchOperations);
    }

    @Override
    public List<T> search(String queryString) {
        Iterable<T> results = search(QueryBuilders.queryStringQuery(queryString));
        return iterableToList(results);
    }

    @Override
    public boolean patch(String id, String painlessQuery) {
        // TODO:
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean patch(String id, String painlessScript, Map<String, Object> params) {
        // TODO:
        throw new UnsupportedOperationException();
    }
}
