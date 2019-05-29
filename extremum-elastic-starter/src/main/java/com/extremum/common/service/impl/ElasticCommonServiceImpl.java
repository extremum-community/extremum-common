package com.extremum.common.service.impl;

import com.extremum.common.dao.ElasticCommonDao;
import com.extremum.common.models.ElasticCommonModel;
import com.extremum.common.service.ElasticCommonService;


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
