package com.extremum.elasticsearch.service.impl;

import com.extremum.common.service.impl.CommonServiceImpl;
import com.extremum.elasticsearch.dao.ElasticsearchCommonDao;
import com.extremum.elasticsearch.model.ElasticsearchCommonModel;
import com.extremum.elasticsearch.service.ElasticsearchCommonService;


public abstract class ElasticsearchCommonServiceImpl<M extends ElasticsearchCommonModel> extends CommonServiceImpl<String, M>
        implements ElasticsearchCommonService<M> {

    protected ElasticsearchCommonServiceImpl(ElasticsearchCommonDao<M> dao) {
        super(dao);
    }

    @Override
    protected String stringToId(String id) {
        return id;
    }

}
