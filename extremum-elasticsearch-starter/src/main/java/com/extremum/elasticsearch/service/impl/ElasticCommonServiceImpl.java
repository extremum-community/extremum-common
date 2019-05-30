package com.extremum.elasticsearch.service.impl;

import com.extremum.common.service.impl.CommonServiceImpl;
import com.extremum.elasticsearch.dao.ElasticCommonDao;
import com.extremum.elasticsearch.model.ElasticCommonModel;
import com.extremum.elasticsearch.service.ElasticCommonService;


public abstract class ElasticCommonServiceImpl<M extends ElasticCommonModel> extends CommonServiceImpl<String, M>
        implements ElasticCommonService<M> {

    protected ElasticCommonServiceImpl(ElasticCommonDao<M> dao) {
        super(dao);
    }

    @Override
    protected String stringToId(String id) {
        return id;
    }

}
