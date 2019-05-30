package com.extremum.elastic.service.impl;

import com.extremum.elastic.dao.ElasticCommonDao;
import com.extremum.elastic.model.ElasticCommonModel;
import com.extremum.elastic.service.ElasticCommonService;


public abstract class ElasticCommonServiceImpl<M extends ElasticCommonModel> extends CommonServiceImpl<String, M>
        implements ElasticCommonService<M> {

    private final ElasticCommonDao<M> dao;

    protected ElasticCommonServiceImpl(ElasticCommonDao<M> dao) {
        super(dao);
        this.dao = dao;
    }

    @Override
    protected String stringToId(String id) {
        return id;
    }

}
