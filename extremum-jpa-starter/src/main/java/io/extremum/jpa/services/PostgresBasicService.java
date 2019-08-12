package io.extremum.jpa.services;


import io.extremum.common.service.CommonService;
import io.extremum.jpa.models.PostgresBasicModel;

/**
 * Common interface for basic posgres/JPA services.
 */
public interface PostgresBasicService<M extends PostgresBasicModel> extends CommonService<M> {
}
