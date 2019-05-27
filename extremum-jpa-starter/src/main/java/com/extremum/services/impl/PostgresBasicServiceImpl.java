package com.extremum.services.impl;

import com.extremum.common.service.impl.CommonServiceImpl;
import com.extremum.dao.PostgresCommonDao;
import com.extremum.models.PostgresBasicModel;
import com.extremum.services.PostgresBasicService;

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
