package com.extremum.services.impl;

import com.extremum.dao.PostgresCommonDao;
import com.extremum.models.PostgresCommonModel;
import com.extremum.services.PostgresCommonService;


public class PostgresCommonServiceImpl<M extends PostgresCommonModel> extends PostgresBasicServiceImpl<M>
        implements PostgresCommonService<M> {

    public PostgresCommonServiceImpl(PostgresCommonDao<M> dao) {
        super(dao);
    }
}
