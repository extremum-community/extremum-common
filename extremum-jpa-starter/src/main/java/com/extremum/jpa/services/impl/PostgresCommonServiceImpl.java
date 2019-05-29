package com.extremum.jpa.services.impl;

import com.extremum.jpa.dao.PostgresCommonDao;
import com.extremum.jpa.services.PostgresCommonService;
import com.extremum.jpa.models.PostgresCommonModel;


public class PostgresCommonServiceImpl<M extends PostgresCommonModel> extends PostgresBasicServiceImpl<M>
        implements PostgresCommonService<M> {

    public PostgresCommonServiceImpl(PostgresCommonDao<M> dao) {
        super(dao);
    }
}
