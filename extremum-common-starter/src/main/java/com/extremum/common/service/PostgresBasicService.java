package com.extremum.common.service;

import com.extremum.common.models.PostgresBasicModel;

import java.util.UUID;

/**
 * Common interface for basic posgres/JPA services.
 */
public interface PostgresBasicService<M extends PostgresBasicModel> extends CommonService<UUID, M> {
}
