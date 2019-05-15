package com.extremum.common.service.impl;

import com.extremum.common.dao.PostgresCommonDao;
import com.extremum.common.models.PostgresCommonModel;
import com.extremum.common.service.PostgresCommonService;

import java.util.UUID;


public class PostgresCommonServiceImpl<M extends PostgresCommonModel> extends CommonServiceImpl<UUID, M>
        implements PostgresCommonService<M> {

    public PostgresCommonServiceImpl(PostgresCommonDao<M> dao) {
        super(dao);
    }

    @Override
    protected UUID stringToId(String id) {
        return UUID.fromString(id);
    }
}
