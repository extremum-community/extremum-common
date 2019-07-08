package com.extremum.common.dto.converters;

import com.extremum.sharedmodels.dto.RequestDto;
import com.extremum.common.models.Model;

public interface FromRequestDtoConverter<M extends Model, D extends RequestDto> extends DtoConverter {
    M convertFromRequest(D dto);

    Class<? extends D> getRequestDtoType();
}
