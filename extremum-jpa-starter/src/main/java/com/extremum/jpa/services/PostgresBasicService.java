package com.extremum.jpa.services;


import com.extremum.elastic.service.CommonService;
import com.extremum.jpa.models.PostgresBasicModel;

import java.util.UUID;

/**
 * Common interface for basic posgres/JPA services.
 */
public interface PostgresBasicService<M extends PostgresBasicModel> extends CommonService<UUID, M> {
}
