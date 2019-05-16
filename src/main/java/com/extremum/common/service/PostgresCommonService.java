package com.extremum.common.service;

import com.extremum.common.models.PostgresCommonModel;

import java.util.UUID;

/**
 * Common interface for posgres/JPA services
 */
public interface PostgresCommonService<M extends PostgresCommonModel> extends CommonService<UUID, M> {
}
