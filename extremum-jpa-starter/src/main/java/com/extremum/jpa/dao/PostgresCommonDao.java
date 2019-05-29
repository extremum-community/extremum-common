package com.extremum.jpa.dao;

import com.extremum.common.dao.CommonDao;
import com.extremum.common.models.BasicModel;

import java.util.UUID;

public interface PostgresCommonDao<M extends BasicModel<UUID>> extends CommonDao<M, UUID> {
}
