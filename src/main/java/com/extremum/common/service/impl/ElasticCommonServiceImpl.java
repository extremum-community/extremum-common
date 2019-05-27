package com.extremum.common.service.impl;

import com.extremum.common.dao.CommonDao;
import com.extremum.common.models.ElasticCommonModel;

public class ElasticCommonServiceImpl<Model extends ElasticCommonModel> extends CommonServiceImpl<String, Model> {
    public ElasticCommonServiceImpl(CommonDao<Model, String> dao) {
        super(dao);
    }

    @Override
    protected String stringToId(String id) {
        return id;
    }
}
