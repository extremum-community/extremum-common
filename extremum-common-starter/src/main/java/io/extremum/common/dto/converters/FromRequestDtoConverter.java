package io.extremum.common.dto.converters;

import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.common.model.Model;

public interface FromRequestDtoConverter<M extends Model, D extends RequestDto> extends DtoConverter {
    M convertFromRequest(D dto);

    Class<? extends D> getRequestDtoType();
}
