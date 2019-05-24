package com.extremum.common.service.impl;

import com.extremum.common.dao.PostgresCommonDao;
import com.extremum.common.models.PostgresCommonModel;
import com.extremum.common.service.PostgresCommonService;


public class PostgresCommonServiceImpl<M extends PostgresCommonModel> extends PostgresBasicServiceImpl<M>
        implements PostgresCommonService<M> {

    public PostgresCommonServiceImpl(PostgresCommonDao<M> dao) {
        super(dao);
    }
}
