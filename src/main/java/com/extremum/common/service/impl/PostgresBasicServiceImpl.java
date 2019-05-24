package com.extremum.common.service.impl;

import com.extremum.common.dao.PostgresCommonDao;
import com.extremum.common.models.PostgresBasicModel;
import com.extremum.common.service.PostgresBasicService;

import java.util.UUID;


public class PostgresBasicServiceImpl<M extends PostgresBasicModel> extends CommonServiceImpl<UUID, M>
        implements PostgresBasicService<M> {

    public PostgresBasicServiceImpl(PostgresCommonDao<M> dao) {
        super(dao);
    }

    @Override
    protected UUID stringToId(String id) {
        return UUID.fromString(id);
    }
}
