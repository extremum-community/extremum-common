package io.extremum.elasticsearch.service.impl;

import io.extremum.common.service.Problems;
import io.extremum.common.service.impl.CommonServiceImpl;
import io.extremum.elasticsearch.dao.ElasticsearchCommonDao;
import io.extremum.elasticsearch.dao.SearchOptions;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import io.extremum.elasticsearch.service.ElasticsearchCommonService;
import reactor.core.publisher.Mono;

import java.util.List;


public abstract class ElasticsearchCommonServiceImpl<M extends ElasticsearchCommonModel> extends CommonServiceImpl<String, M>
        implements ElasticsearchCommonService<M> {

    private final ElasticsearchCommonDao<M> dao;

    protected ElasticsearchCommonServiceImpl(ElasticsearchCommonDao<M> dao) {
        super(dao);

        this.dao = dao;
    }

    @Override
    protected String stringToId(String id) {
        return id;
    }

    @Override
    public List<M> search(String queryString, SearchOptions searchOptions) {
        return dao.search(queryString, searchOptions);
    }

    @Override
    public Mono<M> reactiveGet(String id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Mono<M> reactiveGet(String id, Problems problems) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
