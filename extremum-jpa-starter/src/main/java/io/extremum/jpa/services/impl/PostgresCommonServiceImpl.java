package io.extremum.jpa.services.impl;

import io.extremum.jpa.dao.PostgresCommonDao;
import io.extremum.jpa.services.PostgresCommonService;
import io.extremum.jpa.models.PostgresCommonModel;


public class PostgresCommonServiceImpl<M extends PostgresCommonModel> extends PostgresBasicServiceImpl<M>
        implements PostgresCommonService<M> {

    public PostgresCommonServiceImpl(PostgresCommonDao<M> dao) {
        super(dao);
    }
}
