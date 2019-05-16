package com.extremum.common.dao;

import com.extremum.common.models.PostgresCommonModel;

import java.util.UUID;

public interface PostgresCommonDao<M extends PostgresCommonModel> extends CommonDao <M, UUID> {
}
