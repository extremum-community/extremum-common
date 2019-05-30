package com.extremum.elastic.service.impl;

import com.extremum.common.service.impl.CommonServiceImpl;
import com.extremum.elastic.dao.ElasticCommonDao;
import com.extremum.elastic.model.ElasticCommonModel;
import com.extremum.elastic.service.ElasticCommonService;


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
