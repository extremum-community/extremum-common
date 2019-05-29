package com.extremum.jpa.services.impl;

import com.extremum.elastic.service.impl.CommonServiceImpl;
import com.extremum.jpa.dao.PostgresCommonDao;
import com.extremum.jpa.models.PostgresBasicModel;
import com.extremum.jpa.services.PostgresBasicService;

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
