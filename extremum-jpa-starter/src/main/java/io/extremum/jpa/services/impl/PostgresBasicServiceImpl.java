package io.extremum.jpa.services.impl;

import io.extremum.common.service.impl.CommonServiceImpl;
import io.extremum.jpa.dao.PostgresCommonDao;
import io.extremum.jpa.models.PostgresBasicModel;
import io.extremum.jpa.services.PostgresBasicService;

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
