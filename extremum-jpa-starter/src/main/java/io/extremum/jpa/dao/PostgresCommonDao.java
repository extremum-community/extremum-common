package io.extremum.jpa.dao;

import io.extremum.common.dao.CommonDao;
import io.extremum.common.model.BasicModel;

import java.util.UUID;

public interface PostgresCommonDao<M extends BasicModel<UUID>> extends CommonDao<M, UUID> {
}
